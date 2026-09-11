(ns filing-copying.governor
  "FilingCopyingGovernor — the independent safety/traceability layer
  for the ISCO-08 4415 independent filing-and-copying actor. Wired as
  its own `:govern` node in `filing-copying.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of mandate
  provenance or confidential/retention risk, so this MUST be a
  separate system able to reject a proposal (itonami actor pattern,
  per ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. mandate provenance  — the request's mandate must be registered.
    2. no-actuation        — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per the
  README robotics-premise: handling confidential or legally privileged
  documents, or destroying a document past retention policy, always
  requires human sign-off):
    3. :op :handle-privileged-document.
    4. :op :destroy-past-retention-document.
    5. low confidence (< `confidence-floor`)."
  (:require [filing-copying.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:handle-privileged-document :destroy-past-retention-document})

(defn- hard-violations [{:keys [proposal]} mandate-record]
  (cond-> []
    (nil? mandate-record)
    (conj {:rule :no-mandate :detail "未登録 mandate"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `filing-copying.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [mandate-record (store/mandate store (:mandate-id request))
        hard (hard-violations {:proposal proposal} mandate-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
