# Diagrammes UML - Système de Prise de Rendez-vous Médicaux

Ce répertoire contient les diagrammes UML simplifiés du projet, créés avec PlantUML.

## Liste des Diagrammes

### 1. Architecture Système (`1-architecture-systeme.puml`)
Vue d'ensemble de l'architecture microservices:
- 3 microservices backend (Docteur, RDV, Notification)
- Frontend React
- Bases de données H2
- Services externes (SMS, Email)
- Communications entre services

---

### 2. Modèle de Données (`2-modele-entites.puml`)
Entités principales du système:
- **Docteur:** informations du médecin
- **Rdv:** données du rendez-vous et patient
- Relation entre les deux entités

---

### 3. Séquence - Création de Rendez-vous (`3-sequence-creation-rdv.puml`)
Flux de création d'un rendez-vous:
1. Consultation des docteurs disponibles
2. Sélection et création du rendez-vous
3. Validation via Feign Client
4. Sauvegarde en base de données
5. Envoi de notification

---

### 4. Séquence - Consultation de Rendez-vous (`4-sequence-consultation-rdv.puml`)
Flux de consultation d'un rendez-vous:
1. Récupération des données du rendez-vous
2. Enrichissement avec informations du docteur
3. Affichage complet au patient

---

### 5. Classes - RDV Service (`5-classes-rdv-service.puml`)
Structure du service RDV:
- Entité Rdv
- RdvRepository
- RdvController
- DocteurClient (Feign)

---

### 6. Classes - Docteur Service (`6-classes-docteur-service.puml`)
Structure du service Docteur:
- Entité Docteur
- DocteurRepository (Spring Data REST)
- API auto-générée

---

### 7. Classes - Notification Service (`7-classes-notification-service.puml`)
Structure du service Notification:
- NotificationRequest/Response
- NotificationController
- NotificationService
- WebClient pour SMS/Email

---

### 8. Flux de Données (`9-flux-donnees.puml`)
Flux de données principal:
- Consultation des docteurs
- Création de rendez-vous
- Notifications
- Étapes numérotées

---

## Comment Visualiser les Diagrammes

### Option 1: Extensions VS Code
Installez l'extension **PlantUML** pour VS Code:
```bash
code --install-extension jebbs.plantuml
```

Puis ouvrez n'importe quel fichier `.puml` et utilisez:
- `Alt + D` pour prévisualiser
- Clic droit → "Preview Current Diagram"

### Option 2: PlantUML en ligne
Visitez: https://www.plantuml.com/plantuml/uml/

Copiez-collez le contenu d'un fichier `.puml` et cliquez sur "Submit"

### Option 3: Générer des images PNG/SVG

**Installation de PlantUML:**
```bash
# Ubuntu/Debian
sudo apt-get install plantuml

# macOS
brew install plantuml

# Ou télécharger le JAR depuis http://plantuml.com/download
```

**Générer toutes les images:**
```bash
cd diagrams

# Générer en PNG
plantuml *.puml

# Générer en SVG (vectoriel, meilleure qualité)
plantuml -tsvg *.puml

# Générer en PDF
plantuml -tpdf *.puml
```

### Option 4: Utiliser Docker
```bash
docker run --rm -v $(pwd):/data plantuml/plantuml *.puml
```

---

## Technologies PlantUML Utilisées

- **@startuml / @enduml:** Délimiteurs de diagramme
- **Diagrammes de composants:** `component`, `database`, `cloud`, `package`
- **Diagrammes de séquence:** `actor`, `participant`, `activate`, `alt/else`
- **Diagrammes de classes:** `class`, `interface`, `entity`, `extends`, `implements`
- **Diagrammes de déploiement:** `node`, `component`, `database`
- **Styling:** `skinparam`, `note`, thèmes

---

## Concepts d'Architecture Illustrés

### 1. Architecture Microservices
- Services indépendants et déployables séparément
- Bases de données séparées par service
- Communication via APIs REST

### 2. Patterns Spring
- **IoC/DI:** Injection de dépendances avec @Autowired
- **Repository Pattern:** Spring Data JPA
- **REST API:** @RestController, @RequestMapping
- **Data REST:** API auto-générée pour Docteur Service

### 3. Communication Inter-Services
- **Feign Client:** Communication synchrone REST
- **WebClient:** Communication réactive non-bloquante
- **DTO Pattern:** Transfert de données entre services

### 4. Patterns de Conception
- **MVC:** Model-View-Controller (dans chaque service)
- **Repository:** Abstraction de la couche de persistance
- **DTO:** Data Transfer Objects
- **Service Layer:** Logique métier séparée

---

## Maintenance des Diagrammes

Lors de modifications du code:

1. **Nouveau endpoint** → Mettre à jour les diagrammes de classes et séquence
2. **Nouvelle entité** → Mettre à jour le diagramme d'entités
3. **Nouveau service** → Mettre à jour l'architecture système et déploiement
4. **Nouveau flux métier** → Créer un nouveau diagramme de séquence

---

## Ressources PlantUML

- Documentation officielle: https://plantuml.com/
- Guide de référence rapide: https://plantuml.com/guide
- Galerie d'exemples: https://real-world-plantuml.com/
- Éditeur en ligne: https://www.plantuml.com/plantuml/

---

## Auteur

Diagrammes générés pour le projet d'Architecture des Composants
