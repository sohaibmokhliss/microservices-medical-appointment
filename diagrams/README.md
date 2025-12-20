# Diagrammes UML - Système de Prise de Rendez-vous Médicaux

Ce répertoire contient les diagrammes UML simplifiés du projet, créés avec PlantUML.

## Liste des Diagrammes

### 1. Architecture Système (`1-architecture-systeme.puml`)
Vue d'ensemble de l'architecture microservices:
- API Gateway (Spring Cloud Gateway) - port 8080
- Eureka Server (Service Discovery) - port 8761
- 5 microservices backend (Auth, Docteur, RDV, Notification, Billing)
- Frontend React - port 3000
- Bases de données PostgreSQL (authdb, docteurdb, rdvdb, billingdb)
- RabbitMQ pour messaging asynchrone
- Service externe Resend pour emails
- Communications via Feign Client et RabbitMQ

---

### 2. Modèle de Données (`2-modele-entites.puml`)
Entités principales du système:
- **Docteur:** informations du médecin
- **Rdv:** données du rendez-vous et patient
- **Invoice:** facture générée pour un rendez-vous
- **Payment:** paiement effectué pour une facture
- **Pricing:** tarification par spécialité médicale
- Relations entre les entités

---

### 3. Séquence - Création de Rendez-vous (`3-sequence-creation-rdv.puml`)
Flux de création d'un rendez-vous:
1. Consultation des docteurs via API Gateway
2. Sélection docteur et remplissage formulaire
3. Création via API Gateway -> RDV Service
4. Validation docteur via Feign Client
5. Sauvegarde en base PostgreSQL
6. Publication d'événement dans RabbitMQ
7. Notification Service consomme l'événement de manière asynchrone
8. Envoi email via Resend

---

### 4. Séquence - Consultation de Rendez-vous (`4-sequence-consultation-rdv.puml`)
Flux de consultation des rendez-vous:
1. Requête via API Gateway
2. Récupération de tous les rendez-vous depuis PostgreSQL
3. Enrichissement avec informations docteur via Feign Client
4. Retour via API Gateway
5. Affichage dans le frontend React

---

### 5. Classes - RDV Service (`5-classes-rdv-service.puml`)
Structure du service RDV:
- Entité Rdv
- RdvRepository (Spring Data JPA)
- RdvController (REST API)
- DocteurClient (Feign Client)
- AppointmentEventPublisher (RabbitMQ Publisher)
- AppointmentEvent (événements RDV)

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
- NotificationService
- AppointmentEventListener (RabbitMQ Consumer)
- Integration avec Resend pour emails

---

### 8. Architecture - Interactions Microservices (`8-architecture-interactions.puml`)
Interaction détaillée des microservices:
- API Gateway (port 8080) avec filtre JWT global
- Service Discovery Eureka (port 8761)
- Auth Service (port 8084), Docteur (8083), RDV (8082), Notification (8085), Billing (8086)
- Bases de données PostgreSQL (authdb, docteurdb, rdvdb, billingdb)
- RabbitMQ pour messaging asynchrone
- Resend pour envoi d'emails

---

### 9. Flux de Données (`9-flux-donnees.puml`)
Flux de données principal avec étapes numérotées:
1. Consultation des docteurs via API Gateway -> Docteur Service -> PostgreSQL
2. Création de rendez-vous via API Gateway -> RDV Service
3. Validation via Feign Client
4. Sauvegarde PostgreSQL
5. Publication RabbitMQ
6. Notification asynchrone via Resend

---

### 10. Classes - Auth Service (`10-classes-auth-service.puml`)
Structure du service d'authentification:
- User, LoginRequest/Response, RegisterRequest
- AuthService, JwtUtil, UserRepository
- Endpoints login/validate/me

---

### 11. Classes - API Gateway (`11-classes-api-gateway.puml`)
Structure de la passerelle:
- Filtre global JwtAuthenticationFilter
- Configuration CORS
- Routage vers Auth/Docteur/RDV/Notification

---

### 12. Classes - Billing Service (`12-classes-billing-service.puml`)
Structure du service de facturation:
- Entités Invoice, Payment, Pricing
- InvoiceRepository, PaymentRepository, PricingRepository (Spring Data JPA)
- BillingService (logique métier)
- BillingController (REST API)
- AppointmentEventListener (RabbitMQ Consumer)
- Génération automatique de factures lors de la création de rendez-vous

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
