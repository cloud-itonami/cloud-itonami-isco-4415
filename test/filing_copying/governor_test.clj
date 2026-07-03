(ns filing-copying.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [filing-copying.store :as store]
            [filing-copying.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-mandate! st {:mandate-id "mandate-1" :name "Acme Corp Archive"})
    st))

(deftest ok-on-clean-file
  (let [st (fresh-store)
        proposal {:op :file :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:mandate-id "mandate-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-mandate
  (let [st (fresh-store)
        proposal {:op :file :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:mandate-id "no-such-mandate"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-mandate (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :file :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:mandate-id "mandate-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-privileged-document-handling
  (let [st (fresh-store)
        proposal {:op :handle-privileged-document :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:mandate-id "mandate-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-past-retention-destruction
  (let [st (fresh-store)
        proposal {:op :destroy-past-retention-document :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:mandate-id "mandate-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :file :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:mandate-id "mandate-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:mandate-id "mandate-1" :op :copy})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "mandate-1"))))
    (is (= 1 (count (store/ledger st))))))
