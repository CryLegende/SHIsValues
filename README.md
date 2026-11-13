<div align="center">

<img src="assets/logo.png" alt="SkyHand" width="140">

# SHIsValues

*Affiche les valeurs des blocs de SuperiorSkyblock2 dans un menu.*

<img src="https://img.shields.io/badge/Paper-26.3-f2a900?style=for-the-badge" alt="Paper 26.3"> <img src="https://img.shields.io/badge/Java-25-e76f00?style=for-the-badge" alt="Java 25"> <img src="https://img.shields.io/badge/SkyHand-Opale-3aa655?style=for-the-badge" alt="SkyHand Opale">

</div>

---

## Présentation

Affiche les valeurs des blocs de SuperiorSkyblock2 dans un menu.

Utilisé sur : **Opale**.

## Dépendances

- Obligatoires : `SuperiorSkyblock2`
- Optionnelles : `PlaceholderAPI`

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/shisvalues values` | `/shiv` | Ouvre le menu des valeurs de blocs (joueurs seulement). | `shisvalues.values` |
| `/shisvalues value` | `/shiv` | Ouvre le menu à la page de l'objet tenu en main (joueurs seulement). | `shisvalues.value` |
| `/shisvalues reload` | `/shiv` | Recharge `config.yml`, `messages.yml`, les valeurs de SuperiorSkyblock2 et les noms des permissions d'île ; un fichier invalide annule le rechargement. Marche aussi depuis la console. | `shisvalues.admin.reload` |

Le plugin prend aussi en charge trois sous-commandes de `/is` (SuperiorSkyblock2 traite `/is` normalement pour qui n'a pas le droit) :

| Commande | Description | Permission |
|---|---|---|
| `/is values` | Même menu que `/shisvalues values` | `shisvalues.is.values` |
| `/is value` | Même menu que `/shisvalues value` | `shisvalues.is.value` |
| `/is acces` | Joueurs ayant des permissions particulières sur votre île | `shisvalues.is.acces` |

## Permissions

| Permission | Description | Par défaut |
|---|---|---|
| `shisvalues.values` | `/shisvalues values` | `op` |
| `shisvalues.value` | `/shisvalues value` | `op` |
| `shisvalues.is.values` | `/is values` gérée par le plugin | `op` |
| `shisvalues.is.value` | `/is value` gérée par le plugin | `op` |
| `shisvalues.is.acces` | `/is acces` | `true` |
| `shisvalues.admin` | Toutes les commandes d'administration (parent de la suivante) | `op` |
| `shisvalues.admin.reload` | `/shisvalues reload` | `false` |

## Configuration

Fichiers par défaut, avec des valeurs d'exemple. Cliquez sur un fichier pour l'ouvrir :

<pre>
plugins/SHIsValues/
├── <a href="src/main/resources/config.yml">config.yml</a>
└── <a href="src/main/resources/messages.yml">messages.yml</a>
</pre>

Au premier démarrage avec `messages.yml`, le fichier est créé à partir des sections `messages`, `access-messages` et `access-placeholders` de l'ancien `config.yml` (textes déjà en place), complétées par celles du jar ; ces sections ne sont plus lues ensuite.

## Compilation

Java 25 et Maven :

```bash
mvn clean package
```

Le jar est généré dans `target/`.

---

<div align="center">

<sub>Développé par <b>Cry_Legende</b> pour le serveur <b>SkyHand</b> · Tous droits réservés</sub>

</div>
