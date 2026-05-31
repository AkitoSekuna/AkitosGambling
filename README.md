# AkitosGambling
A gambling plugin featuring fully animated slots and roulette with built-in responsible play protection. Configurable via `settings.yml`; add new slot symbols without touching any code.
## Requirements
- Paper 1.21.1+
- Java 21+
- [AkitosCore](https://github.com/AkitoSekuna/AkitosCore)
## Installation
1. Install [AkitosCore](https://github.com/AkitoSekuna/AkitosCore) first
2. Drop `AkitosGambling.jar` into your `plugins/` folder
3. Restart your server
4. Configure `plugins/AkitosPlugins/AkitosGambling/settings.yml`
## Features
- Animated slot machine with configurable symbols, weights and payout multipliers
- Animated roulette wheel with multiple bet types (red/black/green, even/odd, dozens, specific numbers)
- Loss-streak and win-streak detection with automatic cooldown
- Configurable bet limits, cooldowns and anti-cheat thresholds
- Bet amount persists between sessions
## Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/slots` | Open the slot machine | none |
| `/roulette` | Open the roulette wheel | none |
| `/ag info` | Plugin info | none |
| `/ag reload` | Reload config | `akitosgambling.admin` |
| `/ag history <player>` | View player game history | `akitosgambling.admin` |
| `/ag unflag <player>` | Unflag a flagged player | `akitosgambling.admin` |
## Adding Custom Slot Symbols
Simply add a new entry to `settings.yml`:
```yaml
slots:
  symbols:
    mysymbol:
      material: DIAMOND
      color: "§b"
      weight: 2
      payout-multiplier: 5.0
```
## Part of the Akito's Plugin Network
- [AkitosCore](https://github.com/AkitoSekuna/AkitosCore)
