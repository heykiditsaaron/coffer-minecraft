# Coffer Adapter Config Notes (Temporary)

These notes summarize the current Fabric adapter configs and permission strings. This is a temporary aid; we can move it into proper docs/wiki later.

## Config Files (Fabric)

Location: `config/coffer/`

- `config.yaml` (new consolidated config; specific files still honored for compatibility)  
  Sections:
  - `storage`: `{ type: json|sqlite, path: balances.json|balances.db }`
  - `currency`: default id and list of currencies
  - `blacklist`: `denyTags`, `denyItems`
  - `permissions`: `useFabricPermissionsApi` and optional `defaults` map for op-level fallbacks per permission node
  Commented example is emitted by default for guidance.

- `valuation.json`  
  Format: `{ "rules": [ { "type": "item|tag|default", "id": "<item or tag id>", "currency": "<currencyId>", "value": <positive long>, "priority": <int> } ] }` (JSON only; no comments).  
  Resolution: pick highest `priority` rule that matches currency; ties break by specificity (`item` > `tag` > `default`). Values are minor units of the currency. Missing/<=0 value means ignored. Legacy flat maps are still accepted and treated as item rules with priority 0 and the default currency.

- `blacklist.yaml` (JSON still accepted)  
  Format:  
  ```yaml
  denyTags: ["minecraft:planks"]
  denyItems: ["minecraft:bedrock"]
  ```  
  Behavior: any item matching denyTags or denyItems is denied by adapter policy before Core. Missing/empty means nothing is blacklisted.

- `storage.yaml` (JSON still accepted)  
  Format:  
  ```yaml
  type: json # or sqlite
  path: balances.json # for sqlite, a .db filename/path
  ```  
  Behavior: JSON uses a simple file under `config/coffer/`. SQLite (new) uses a bundled JDBC-backed db file under `config/coffer/` (or your provided path). If storage cannot be read/written, the adapter refuses READY and surfaces a storage error; all operations are denied when storage is unavailable. Schema: table `balances(account TEXT, currency TEXT, balance INTEGER, PRIMARY KEY(account,currency))` (designed to be portable to other JDBC backends later).
  Default in the bundled `config.yaml` template is `sqlite` with `balances.db`. Migration from legacy JSON, if desired, must be an explicit admin action (no automatic import).

- `currency.yaml` (JSON still accepted for compatibility)  
  Format:  
  ```yaml
  default: coins
  currencies:
    - id: coins
      name: Coin
      plural: Coins
      symbol: ""
      decimals: 0
  ```  
  Behavior: defines the default currency label/symbol/decimals for formatting balances, valuations, and shop prices. Amounts in configs and storage are interpreted as the currency's minor unit (respecting `decimals`). Zero-config boots with the `coins` default above. Multiple currencies can be declared; commands and storage currently operate using the configured `default` currency id.

- `shops/*.json` (per-shop files under `config/coffer/shops/`)  
  Format:  
  ```json
  {
    "name": "Spawn Logs",
    "entries": [
      { "target": "minecraft:oak_log", "kind": "item", "multiplier": 2.0, "additive": 0 },
      { "target": "minecraft:logs", "kind": "tag", "multiplier": 1.5, "additive": 0 }
    ]
  }
  ```  
  Behavior: modifiers apply only to admin shop pricing. Adjusted price = base * multiplier + additive; if base is missing or adjusted <= 0, the item is denied for that shop.

## Commands & Permissions (Fabric Permissions API if present; fallback to op levels)

- `/sell`  
  Permission: `coffer.command.sell.execute` (default op 0)

- `/coffer`, `/ready` (adapter ready check)  
  Permission: `coffer.command.ready` (default op 0)

- `/coffer audits`, `/audits`  
  Permission: `coffer.command.audits` (default op 2)

- `/coffer storage import-json <path>` (admin-only, audited migration)  
  Permission: `coffer.command.storage` (default op 3)

- `/shop open <shopId>` (diagnostic, lists entries)  
  Permission: `coffer.command.shop.open` (default op 0)
- `/shop price <shopId> <item> [quantity]`  
  Permission: `coffer.command.shop.price` (default op 0)
- `/shop buy <shopId> <item> <quantity>`  
  Permission: `coffer.command.shop.buy` (default op 0)

- `/balance` (self)  
  Permission: `coffer.command.balance.self` (default op 0)

- `/balance <player>` (others)  
  Permission: `coffer.command.balance.others` (default op 2)

- `/credit <player> <amount>`  
  Permission: `coffer.command.credit` (default op 2)

- `/debit <player> <amount> [currency]`  
  Permission: `coffer.command.debit` (default op 2)

Notes:
- Commands use Fabric Permissions API if installed (LuckPerms-compatible). Otherwise, they fall back to vanilla op-level checks as noted.
- Op-level fallbacks can be overridden in `config.yaml` under `permissions.defaults`.
- Balances are persisted via JSON store (default `config/coffer/balances.json`).
- `/coffer reload` now reloads configs transactionally: it refuses during reload, swaps services only if all configs parse and storage opens, and keeps the previous state if reload fails (with an explicit error message).
- Startup logs announce currency, valuation counts, blacklist counts, shop count, and storage path after successful init/reload.

## Current Behavior Snapshot

- Policy: denies non-SELL payloads and blacklisted items; otherwise allows through to Core.
- Valuation: deny-by-default unless explicitly configured with positive values; zero-config is valid and denies all.
- Sell: single-step, uses declaration → Core eval → planning → atomic execution; calm refusals.
- Audit: rolling buffer (default 50) visible via `/coffer audits` / `/audits`.
