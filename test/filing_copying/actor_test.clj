(ns filing-copying.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [filing-copying.actor :as actor]
            [filing-copying.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-mandate! st {:mandate-id "mandate-1" :name "Acme Corp Archive"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:mandate-id "mandate-1" :op :file :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "mandate-1"))))))

(deftest holds-on-unregistered-mandate-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:mandate-id "no-such-mandate" :op :file :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-mandate")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; privileged-document handling always escalates (governor invariant)
        request {:mandate-id "mandate-1" :op :handle-privileged-document :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "mandate-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "mandate-1")))))))
