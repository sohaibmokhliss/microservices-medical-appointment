# Documentation LaTeX Condensée du Projet

Ce dossier contient une version **condensée** (25-30 pages) de la documentation académique du projet de système de prise de rendez-vous médical basé sur une architecture microservices.

## 📄 Version Condensée vs Version Complète

- **Version condensée** (ce dossier) : Environ 25-30 pages
  - Résumé des sections principales
  - Tables synthétiques pour information compacte
  - Captures d'écran clés uniquement
  - Focus sur l'essentiel du projet

- **Version complète** (dossier `docs/`) : Environ 92 pages
  - Documentation détaillée avec tous les diagrammes
  - Analyses approfondies des alternatives
  - Exemples de code complets
  - Tests et scénarios détaillés

> **Note**: Pour la documentation complète et détaillée, consultez le dossier `docs/` à la racine du projet.

## Structure

```
docs/short/
├── main.tex                    # Document LaTeX condensé (tout en un)
└── README.md                   # Ce fichier
```

## Contenu de la Version Condensée

### Pages préliminaires
- Page de garde (avec mention de la version complète)
- Remerciements
- Résumé (FR) + Abstract (EN)
- Table des matières
- Liste des figures
- Liste des tableaux
- Liste des acronymes

### Chapitres condensés

1. **Introduction Générale** (1 page)
   - Contexte simplifié
   - Problématique et objectifs principaux
   - Organisation du rapport

2. **Étude de l'Existant et Analyse des Besoins** (3-4 pages)
   - Identification des acteurs
   - Tableau comparatif des solutions existantes
   - Besoins fonctionnels et non-fonctionnels en tableaux

3. **Architecture Générale** (3-4 pages)
   - Principes microservices essentiels
   - Diagramme d'architecture
   - Tableau récapitulatif des composants
   - Résumé des patterns de communication

4. **Conception** (3-4 pages)
   - Modèle d'entités (diagramme)
   - Résumé textuel des services (sans diagrammes de classes détaillés)

5. **Choix Technologiques** (2-3 pages)
   - Tableau unique avec technologies et justifications
   - Pas d'analyse comparative détaillée

6. **Réalisation** (4-5 pages)
   - Captures d'écran principales (login, formulaire RDV, facturation)
   - Tableaux récapitulatifs des endpoints REST
   - Défis techniques principaux

7. **Tests et Validation** (2-3 pages)
   - Tableaux de synthèse des tests
   - Métriques de couverture

8. **Résultats et Discussion** (2-3 pages)
   - Tableau de conformité
   - Résumé des limitations

9. **Conclusion et Perspectives** (1-2 pages)
   - Bilan succinct
   - Perspectives à court terme uniquement

### Bibliographie
- Références essentielles

## Compilation du Document

### Prérequis

Une distribution LaTeX installée :

- **Linux** : TeX Live
  ```bash
  sudo apt-get install texlive-full
  ```

- **macOS** : MacTeX
  ```bash
  brew install --cask mactex
  ```

- **Windows** : MiKTeX ou TeX Live
  - Télécharger depuis https://miktex.org/ ou https://www.tug.org/texlive/

### Compilation

1. Naviguez vers le dossier docs/short :
   ```bash
   cd docs/short/
   ```

2. Compilez le document (2 passages pour les références) :
   ```bash
   pdflatex main.tex
   pdflatex main.tex
   ```

3. Le fichier PDF généré sera : `main.pdf`

### Compilation complète

Pour une compilation complète avec toutes les références et la table des matières :

```bash
pdflatex main.tex
pdflatex main.tex
pdflatex main.tex
```

### Nettoyage des fichiers temporaires

Après compilation, supprimez les fichiers temporaires :

```bash
rm -f *.aux *.log *.out *.toc *.lof *.lot *.bbl *.blg
```

## Utilisation avec un éditeur LaTeX

### Overleaf (en ligne)

1. Créez un nouveau projet sur https://www.overleaf.com/
2. Téléversez le fichier `main.tex`
3. Téléversez également les images depuis `../figures/` (dossier parent)
4. Compilez (bouton "Recompile")

### TeXstudio (desktop)

1. Installez TeXstudio : https://www.texstudio.org/
2. Ouvrez `main.tex`
3. Appuyez sur F5 ou cliquez sur "Build & View"

### VS Code avec LaTeX Workshop

1. Installez l'extension "LaTeX Workshop"
2. Ouvrez le dossier `docs/short/` dans VS Code
3. Ouvrez `main.tex`
4. Ctrl+Alt+B pour compiler

## Caractéristiques Techniques

- **Classe de document** : report
- **Taille de police** : 12pt
- **Format de papier** : A4
- **Marges** : Réduites par rapport à la version complète (Gauche/Droite 2.5cm, Haut/Bas 2cm)
- **Interligne** : 1.5 (onehalfspacing)
- **Langue principale** : Français
- **Encodage** : UTF-8

## Différences avec la Version Complète

| Aspect | Version Condensée | Version Complète |
|--------|-------------------|------------------|
| Nombre de pages | 25-30 pages | ~92 pages |
| Structure | Fichier unique | Fichiers modulaires |
| Détails techniques | Résumés en tableaux | Analyses détaillées |
| Diagrammes | Essentiels uniquement | Tous les diagrammes |
| Code source | Aucun | Exemples complets |
| Captures d'écran | Principales (3-4) | Toutes (9+) |
| Analyses comparatives | Tableaux synthétiques | Comparaisons détaillées |
| Perspectives | Court terme | Court, moyen et long terme |

## Images et Figures

Les figures sont référencées depuis le dossier parent `../figures/` pour éviter la duplication :

```latex
\graphicspath{{../figures/}}
```

Cela permet d'utiliser les mêmes images que la version complète.

## Longueur Cible

Le document compilé doit faire environ **25-30 pages** (incluant les pages préliminaires).

## Conseils d'Utilisation

- Pour une présentation : utilisez cette version condensée
- Pour une lecture approfondie : consultez la version complète dans `docs/`
- Pour une soumission académique : vérifiez les exigences (certaines institutions préfèrent les versions complètes)

## Auteur

**Sohaib Mokhliss**  
Encadré par : Pr. Abdelaziz Ettaoufik  
Année Universitaire 2024-2025

## Support

Pour toute question concernant la compilation ou le contenu :
- Documentation LaTeX : https://www.latex-project.org/help/documentation/
- Forum TeX Stack Exchange : https://tex.stackexchange.com/
