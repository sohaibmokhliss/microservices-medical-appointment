# Documentation LaTeX du Projet

Ce dossier contient la documentation académique complète du projet de système de prise de rendez-vous médical basé sur une architecture microservices.

## Structure

```
docs/
├── main.tex                          # Fichier principal LaTeX
├── chapters/                         # Chapitres du rapport
│   ├── introduction.tex
│   ├── chapitre1_existant_besoins.tex
│   ├── chapitre2_architecture.tex
│   ├── chapitre3_conception.tex
│   ├── chapitre4_choix_technologiques.tex
│   ├── chapitre5_realisation.tex
│   ├── chapitre6_tests_validation.tex
│   ├── chapitre7_resultats_discussion.tex
│   └── conclusion_perspectives.tex
└── figures/                          # Figures et images
    ├── diagrams/                     # Diagrammes UML et architecture
    │   ├── 1-architecture-systeme.png
    │   ├── 2-modele-entites.png
    │   ├── 3-classes-rdv-service.png
    │   ├── 4-classes-docteur-service.png
    │   ├── 5-classes-notification-service.png
    │   ├── 6-flux-donnees.png
    │   ├── 7-classes-auth-service.png
    │   └── 8-classes-billing-service.png
    └── ui/                           # Captures d'écran de l'interface
        ├── admin-add-new-receptionist-form.png
        ├── admin-doctor-management-table.png
        ├── admin-receptionist-management-table.png
        ├── appointment-booking-form-success.png
        ├── gmail-appointment-confirmation-emails.png
        ├── login-page-medical-appointment-system.png
        ├── my-appointments-list-with-status.png
        ├── receptionist-billing-and-invoices-management.png
        └── receptionist-doctors-list-view.png
```

## Contenu du Rapport

Le rapport comprend les sections suivantes :

### Pages préliminaires
- Page de garde
- Remerciements
- Résumé (FR) + Abstract (EN)
- Table des matières
- Liste des figures
- Liste des tableaux
- Liste des acronymes

### Chapitres
1. **Introduction générale** - Contexte, problématique, objectifs
2. **Étude de l'existant et analyse des besoins** - Contexte e-Santé, acteurs, solutions existantes, besoins
3. **Architecture générale du système** - Principes microservices, vue d'ensemble, communication inter-services
4. **Conception du système** - Modèles de données, diagrammes de classes UML, patterns
5. **Choix technologiques** - Justification des technologies (Spring Boot, React, PostgreSQL, RabbitMQ)
6. **Réalisation et implémentation** - Captures d'écran, endpoints REST, défis techniques
7. **Tests et validation** - Stratégie de tests, résultats, métriques
8. **Résultats et discussion** - Analyse critique, points forts, limitations
9. **Conclusion et perspectives** - Bilan, apports, évolutions futures

### Bibliographie
Références complètes (Spring, React, RabbitMQ, ouvrages sur les microservices)

## Compilation du Document

### Prérequis

Pour compiler le document LaTeX, vous avez besoin d'une distribution LaTeX installée :

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

1. Naviguez vers le dossier docs :
   ```bash
   cd docs/
   ```

2. Compilez le document (2 passages pour les références) :
   ```bash
   pdflatex main.tex
   pdflatex main.tex
   ```

3. Le fichier PDF généré sera : `main.pdf`

### Compilation avancée

Pour une compilation complète avec toutes les références et la table des matières :

```bash
pdflatex main.tex
pdflatex main.tex
pdflatex main.tex
```

### Nettoyage des fichiers temporaires

Après compilation, vous pouvez supprimer les fichiers temporaires :

```bash
rm -f *.aux *.log *.out *.toc *.lof *.lot *.bbl *.blg
```

## Utilisation avec un éditeur LaTeX

### Overleaf (en ligne)

1. Créez un nouveau projet sur https://www.overleaf.com/
2. Téléversez tous les fichiers du dossier `docs/`
3. Définissez `main.tex` comme fichier principal
4. Compilez (le bouton "Recompile" dans Overleaf)

### TeXstudio (desktop)

1. Installez TeXstudio : https://www.texstudio.org/
2. Ouvrez `main.tex`
3. Appuyez sur F5 ou cliquez sur "Build & View"

### VS Code avec LaTeX Workshop

1. Installez l'extension "LaTeX Workshop"
2. Ouvrez le dossier `docs/` dans VS Code
3. Ouvrez `main.tex`
4. Ctrl+Alt+B pour compiler

## Personnalisation

### Informations de l'université

Modifiez les lignes suivantes dans `main.tex` (page de titre) :

```latex
{\large Université [Nom de l'Université]\par}
{\large École [Nom de l'École]\par}
{\large Département [Nom du Département]\par}
```

### Ajout de contenu

Pour ajouter du contenu, modifiez les fichiers `.tex` dans le dossier `chapters/`.

### Ajout de figures

1. Placez vos images dans `figures/diagrams/` ou `figures/ui/`
2. Référencez-les dans le texte :
   ```latex
   \begin{figure}[H]
       \centering
       \includegraphics[width=0.8\textwidth]{diagrams/votre-image.png}
       \caption{Description de votre image}
       \label{fig:votre-label}
   \end{figure}
   ```
3. Référencez la figure dans le texte : `Figure~\ref{fig:votre-label}`

## Caractéristiques Techniques

- **Classe de document** : report
- **Taille de police** : 12pt
- **Format de papier** : A4
- **Marges** : Gauche 3cm, Droite 2.5cm, Haut/Bas 2.5cm
- **Interligne** : 1.5 (onehalfspacing)
- **Langue principale** : Français
- **Encodage** : UTF-8

## Packages LaTeX Utilisés

- `inputenc` : Encodage UTF-8
- `babel` : Support du français
- `graphicx` : Inclusion d'images
- `geometry` : Configuration des marges
- `hyperref` : Liens hypertextes et PDF
- `listings` : Coloration syntaxique du code
- `float` : Positionnement des figures
- `acronym` : Liste des acronymes
- `fancyhdr` : En-têtes et pieds de page personnalisés

## Longueur Estimée

Le document compilé fait environ **20-25 pages** (hors pages préliminaires).

## Diagrammes Inclus

Tous les diagrammes suivants sont inclus et référencés dans le rapport :

### Diagrammes d'architecture et conception
- Architecture générale du système
- Modèle d'entités
- Diagramme de classes - RDV Service
- Diagramme de classes - Docteur Service
- Diagramme de classes - Notification Service
- Flux de données
- Diagramme de classes - Auth Service
- Diagramme de classes - Billing Service

### Captures d'écran de l'interface utilisateur
- Page de connexion
- Gestion des réceptionnistes (liste et formulaire)
- Gestion des médecins (admin et réceptionniste)
- Formulaire de prise de rendez-vous
- Liste des rendez-vous
- Emails de confirmation
- Gestion de la facturation

## Licence

Ce document est fourni à des fins académiques et de documentation du projet.

## Auteur

**Sohaib Mokhliss**  
Encadré par : Pr. Abdelaziz Ettaoufik  
Année Universitaire 2025-2026

## Support

Pour toute question concernant la compilation ou le contenu du document, veuillez vous référer à :
- Documentation LaTeX : https://www.latex-project.org/help/documentation/
- Forum TeX Stack Exchange : https://tex.stackexchange.com/
