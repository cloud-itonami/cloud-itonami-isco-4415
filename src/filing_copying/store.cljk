(ns filing-copying.store
  "SSoT for the ISCO-08 4415 independent filing-and-copying
  sole-proprietor actor. Store is a protocol injected into the
  `filing-copying.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    mandate  — a registered client mandate (:mandate-id, :name)
    record   — a committed operating record under a mandate (filed
               document, copied document, privileged-document
               handling, past-retention destruction) — written ONLY
               via commit-record!, never mutated in place
    ledger   — an append-only audit trail of every proposal/verdict/
               disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (mandate [s mandate-id])
  (records-of [s mandate-id])
  (ledger [s])
  (register-mandate! [s mandate])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (mandate [_ mandate-id] (get-in @a [:mandates mandate-id]))
  (records-of [_ mandate-id] (filter #(= mandate-id (:mandate-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-mandate! [s mandate]
    (swap! a assoc-in [:mandates (:mandate-id mandate)] mandate) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:mandates {} :records [] :ledger []} seed)))))
