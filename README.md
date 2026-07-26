# AkitosGambling

Slots and roulette gambling plugin with fully animated games and built-in responsible-play protection. Configurable through `settings.yml`; new slot symbols can be added without touching code.

## Requirements

- Paper 1.21.1+
- Java 21+
- AkitosCore v21.2.0+

## Installation

1. Install AkitosCore first.
2. Drop `AkitosGambling.jar` into your `plugins/` folder.
3. Restart the server.
4. Configure `plugins/AkitosPlugins/AkitosGambling/settings.yml`.

## Features

- Animated slot machine with configurable symbols, weights, and payout multipliers
- Animated roulette wheel with multiple bet types (red/black/green, even/odd, dozens, specific numbers)
- Loss-streak and win-streak detection with automatic cooldown
- Configurable bet limits, cooldowns, and anti-cheat thresholds
- Bet amount persists between sessions

## Commands

| Command | Description | Permission |
|---|---|---|
| `/slots` | Open the slot machine | none |
| `/roulette` | Open the roulette wheel | none |
| `/ag info` | Show plugin info | none |
| `/ag reload` | Reload config | `akitosgambling.admin` |
| `/ag history <player>` | View a player's game history | `akitosgambling.admin` |
| `/ag unflag <player>` | Unflag a player flagged by anti-cheat detection | `akitosgambling.admin` |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `akitosgambling.admin` | Access to reload, history, and unflag subcommands | op |

## Configuration

`plugins/AkitosPlugins/AkitosGambling/settings.yml` controls slot symbols, payouts, bet limits, and anti-cheat thresholds. Example slot symbol entry:

```yaml
slots:
  symbols:
    mysymbol:
      material: DIAMOND
      color: "&b"
      weight: 2
      payout-multiplier: 5.0
```

| Key | Type | Description |
|---|---|---|
| `material` | string | Bukkit material used to represent the symbol |
| `color` | string | Color code applied to the symbol's display name |
| `weight` | integer | Relative chance of the symbol appearing on a reel |
| `payout-multiplier` | double | Multiplier applied to the bet on a matching payline |

[NOTE: bet limit, cooldown, and anti-cheat threshold keys are documented as comments directly in `settings.yml` and are not repeated here.]

## Adding Custom Slot Symbols

Add a new entry under `slots.symbols` in `settings.yml`. No code changes or recompilation are required.

## Part of the Akitos Plugin Network

- [AkitosCore](https://github.com/AkitoSekuna/AkitosCore) (required)
