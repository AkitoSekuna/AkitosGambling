# AkitosGambling

Casino-style gambling plugin for the Akitos network. Animated slot machine and roulette wheel, with built-in win/loss-streak flagging.

## Requirements

- Paper 1.21.11
- Java 21+
- AkitosCore [21.2.0, 21.3.0)
- Vault

## Installation

1. Install AkitosCore and Vault first.
2. Drop `AkitosGambling.jar` into your `plugins/` folder.
3. Restart the server.
4. Configure `plugins/AkitosPlugins/AkitosGambling/config.yml` if you want to change bet limits, cooldowns, or anti-cheat behavior.

## Features

- Animated slot machine with 8 fixed symbols, each with its own spin weight and payout multiplier
- Animated roulette wheel with 8 bet types (red, black, green, even, odd, 1st/12/2nd dozen, single number)
- Green roulette wins of 500+ grant the LuckPerms group `ludoman`, if LuckPerms is installed (silently skipped otherwise)
- Roulette wager is refunded in full if a player disconnects mid-spin (see `RouletteListener.onQuit()`)
- Win-streak and loss-streak detection that can temporarily flag a player, with admin override
- Configurable bet limits, cooldowns, and (once added to config) anti-cheat thresholds

## Commands

| Command | Description | Permission |
|---|---|---|
| `/slots` | Open the slot machine | none |
| `/roulette` | Open the roulette wheel | none |
| `/ag` | Show plugin info | none |
| `/ag info` | Show plugin info | none |
| `/ag reload` | Reload config | `akitosgambling.admin` |
| `/ag history <player>` | View a player's notable game history | `akitosgambling.admin` |
| `/ag unflag <player>` | Clear a player's win/loss-streak flag | `akitosgambling.admin` |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `akitosgambling.admin` | Access to the `/ag` command (info, reload, history, unflag) | op |

## Configuration

`plugins/AkitosPlugins/AkitosGambling/config.yml`:

```yaml
slots:
  min-bet: 4.0
  max-bet: 500.0
  cooldown-seconds: 3

roulette:
  min-bet: 5.0
  max-bet: 1000.0
  cooldown-seconds: 6
  spin-duration-seconds: 5
```

| Key | Type | Description |
|---|---|---|
| `slots.min-bet` / `slots.max-bet` | double | Bet range for `/slots` |
| `slots.cooldown-seconds` | integer | Cooldown between slot spins |
| `roulette.min-bet` / `roulette.max-bet` | double | Bet range for `/roulette` |
| `roulette.cooldown-seconds` | integer | Cooldown between roulette spins |
| `roulette.spin-duration-seconds` | integer | How long the roulette wheel animation runs |

### Slot symbols are not config-driven

Unlike bet limits, the 8 slot symbols, their spin weights, and their payout multipliers are hardcoded in `SlotsConfig.java`, not read from `config.yml`. Changing them requires editing that file and rebuilding:

| Symbol | Weight | Payout |
|---|---|---|
| Cherry | 30 | x1.0 |
| Lemon | 25 | x2.0 |
| Orange | 20 | x3.0 |
| Plum | 12 | x4.0 |
| Bell | 8 | x8.0 |
| Bar | 6 | x10.0 |
| Seven | 4 | x15.0 |
| Jackpot | 1 | x50.0 |

Higher weight means more common; all 8 always compete against each other (weights don't need to sum to 100).

### Roulette bet types

| Bet | Payout |
|---|---|
| Red / Black | x2 |
| Even / Odd | x2 |
| 1st Dozen (1-12) / 2nd Dozen (13-24) | x2 |
| Green (0/00) | x10 |
| Single Number | x20 |

### Anti-cheat thresholds

`GameHistoryManager` reads these directly from `config.yml` under an `anti-cheat:` section, but that section is not present in the shipped default config, so every server currently runs on these hardcoded fallbacks unless an admin adds the section manually:

```yaml
anti-cheat:
  notable-payout-multiplier: 3.0
  history-size: 10
  flag-win-streak: 5
  flag-loss-streak: 10
  flag-cooldown-hours: 24
  messages-flagged-winning: []
  messages-flagged-losing: []
```

Only losses, and wins with a payout ratio at or above `notable-payout-multiplier`, are recorded to a player's history; `history-size` caps how many entries are kept. A run of `flag-win-streak` notable wins or `flag-loss-streak` losses in a row flags the player, blocking further spins until `flag-cooldown-hours` passes or an admin runs `/ag unflag`. The `messages-flagged-*` lists are shown to a flagged player at random; if empty, a default message is used.

## Part of the Akitos Plugin Network

- [AkitosCore](https://github.com/AkitoSekuna/AkitosCore) (required)
