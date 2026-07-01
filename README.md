# cloud-itonami-isco-4415

Open Occupation Blueprint for **ISCO-08 4415**: Filing and Copying Clerks.

This repository designs a forkable OSS business for an independent filing and copying clerk: a filing-and-copying robot performs physical document sorting and copying under a governor-gated actor, so the practice keeps its own handling and retention records instead of renting a closed document-management SaaS.

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs
the physical domain work**. Here a filing-and-copying robot performs physical document sorting, copying and archive placement under an actor that proposes
actions and an independent **Filing Copying Governor** that gates them. The governor never
dispatches hardware itself; `:high`/`:safety-critical` actions (such as
handling confidential or legally privileged documents, or destroying a document past retention policy) require human sign-off.

A live sample of the operator console (robotics safety console, shared template) is rendered in [docs/samples/operator-console.html](docs/samples/operator-console.html) — pure-data HTML output of `kotoba.robotics.ui`.

## Core Contract

```text
client mandate + document scope + retention policy
        |
        v
Filing Advisor -> Filing Copying Governor -> file/copy, or human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated advice can dispatch a robot action the governor refuses, suppress
an operating record, or disclose sensitive data without governor approval and
audit evidence.

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISCO-08 `4415`). Required capabilities:

- :robotics
- :forms
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## License

AGPL-3.0-or-later.
