# Système de Prise de Rendez-vous Médicaux en Ligne

Application web complète de gestion de rendez-vous médicaux utilisant une **architecture microservices production-ready** avec Spring Boot, React, et messaging asynchrone.

## 📑 Table des Matières

- [Fonctionnalités Clés](#-fonctionnalités-clés)
- [Architecture](#architecture)
- [Technologies Utilisées](#technologies-utilisées)
- [Services](#services)
- [Installation et Démarrage](#installation-et-démarrage)
- [Utilisation](#utilisation)
- [Structure du Projet](#structure-du-projet)
- [Patterns et Concepts Implémentés](#patterns-et-concepts-implémentés)
- [Configuration](#configuration)
- [Scripts Utilitaires](#scripts-utilitaires)
- [Monitoring et Logs](#monitoring-et-logs)
- [Tests](#tests)
- [Documentation](#documentation)
- [Screenshots](#screenshots)
- [Évolutions Futures](#évolutions-futures)
- [Contributing](#contributing)
- [Auteur](#auteur)
- [Remerciements](#remerciements)

## 🎯 Fonctionnalités Clés

- ✅ **Architecture Microservices** - 6 services backend indépendants + infrastructure
- ✅ **API Gateway** - Point d'entrée unique avec Spring Cloud Gateway (port 8080)
- ✅ **Service Discovery** - Eureka pour l'enregistrement et découverte automatique des services
- ✅ **Authentification JWT** - Sécurité avec contrôle d'accès basé sur les rôles (Admin/User)
- ✅ **Resilience Patterns** - Circuit Breaker, Retry, Timeout pour la tolérance aux pannes
- ✅ **Communication Asynchrone** - RabbitMQ pour les événements et notifications
- ✅ **Base de Données PostgreSQL** - Persistance fiable avec 4 bases distinctes
- ✅ **Gestion des Exceptions Globale** - Validation et messages d'erreur cohérents
- ✅ **Notifications Email** - Intégration avec Resend pour emails transactionnels
- ✅ **Système de Facturation** - Génération automatique de factures et gestion des paiements
- ✅ **Accès Public** - Création de rendez-vous sans authentification

## Architecture

Le système est composé de **6 microservices backend** + infrastructure:

```
┌──────────────────┐
│  React Frontend  │
│   (Port 3000)    │
└────────┬─────────┘
         │
         v
┌──────────────────┐
│   API Gateway    │
│  (Port 8080)     │
│  - JWT Filter    │
│  - CORS          │
│  - Routing       │
└────────┬─────────┘
         │
    ┌────┴────┬────────────┬──────────────┬─────────────┬─────────────┐
    │         │            │              │             │             │
    v         v            v              v             v             v
┌─────────┐ ┌──────────┐ ┌───────────┐ ┌────────────┐ ┌──────────┐ ┌──────────────┐
│  Auth   │ │ Docteur  │ │    RDV    │ │Notification│ │ Billing  │ │Eureka Server │
│ Service │ │ Service  │ │  Service  │ │  Service   │ │ Service  │ │   (8761)     │
│ (8084)  │ │  (8081)  │ │  (8082)   │ │   (8083)   │ │  (8085)  │ └──────────────┘
└────┬────┘ └────┬─────┘ └─────┬─────┘ └─────┬──────┘ └────┬─────┘
     │           │              │             │             │
     │           │              │             │             │
     v           v              v             v             v
┌─────────┐ ┌──────────┐ ┌───────────┐     ┌──────────┐ ┌──────────┐
│PostgreSQL│ │PostgreSQL│ │PostgreSQL │     │ RabbitMQ │ │PostgreSQL│
│ authdb  │ │docteurdb │ │  rdvdb    │     └─────┬────┘ │billingdb │
└─────────┘ └──────────┘ └─────┬─────┘           │      └──────────┘
                                │                 │
                                └─────────────────┘
                                  (Events async)
```

### Communication Inter-Services

- **Frontend → API Gateway** : HTTP/HTTPS avec JWT optionnel
- **API Gateway → Services** : Routage avec headers JWT propagés
- **RDV → Docteur** : Feign Client (synchrone)
- **RDV → Notification** : RabbitMQ (asynchrone)
- **RDV → Billing** : RabbitMQ (asynchrone)
- **Billing → Notification** : RabbitMQ (asynchrone)
- **Services → Eureka** : Enregistrement et discovery

## Technologies Utilisées

### Backend
- **Spring Boot 3.2.0** - Framework Java pour les microservices
- **Spring Cloud Gateway** - API Gateway avec filtres JWT et routage
- **Spring Cloud Netflix Eureka** - Service Discovery et Load Balancing
- **Spring Security + JWT** - Authentification et autorisation
- **Spring Cloud OpenFeign** - Client REST déclaratif (RDV → Docteur)
- **Spring AMQP + RabbitMQ** - Messaging asynchrone pour notifications
- **Spring Data JPA** - Persistance des données
- **PostgreSQL** - Base de données relationnelle
- **Resilience4j** - Circuit Breaker, Retry, Timeout
- **Lombok** - Réduction du code boilerplate
- **Jackson JSR310** - Sérialisation LocalDateTime

### Frontend
- **React 18.2.0** - Bibliothèque JavaScript pour l'interface utilisateur
- **Axios** - Client HTTP pour les appels API
- **JWT Decode** - Décodage et gestion des tokens JWT
- **CSS3** - Styles personnalisés

### Infrastructure
- **PostgreSQL** - Bases de données (authdb, docteurdb, rdvdb, billingdb)
- **RabbitMQ** - Message broker pour événements asynchrones
- **Docker** - Conteneurisation de RabbitMQ

## Services

### 1. Eureka Server (Port 8761)
**Service Discovery**
- Enregistrement automatique de tous les microservices
- Load balancing et health checking
- Dashboard : http://localhost:8761

### 2. API Gateway (Port 8080)
**Point d'entrée unique**
- Routage vers tous les services
- Filtre JWT global avec endpoints publics configurables
- CORS configuré pour localhost:3000
- Propagation des headers d'authentification

**Routes:**
- `/api/auth/**` → Auth Service
- `/api/docteurs/**` → Docteur Service (public)
- `/api/rdv/**` → RDV Service (public pour création, protégé pour modification)
- `/api/notifications/**` → Notification Service
- `/api/billing/**` → Billing Service

### 3. Auth Service (Port 8084)
**Authentification et Gestion des Utilisateurs**

**Endpoints:**
- `POST /api/auth/register` - Inscription
- `POST /api/auth/login` - Connexion (retourne JWT)
- `GET /api/auth/validate` - Valider un token
- `GET /api/auth/me` - Profil utilisateur actuel
- `GET /api/users` - Liste des utilisateurs (Admin)
- `PUT /api/users/{id}` - Modifier un utilisateur (Admin)
- `DELETE /api/users/{id}` - Supprimer un utilisateur (Admin)

**Rôles:**
- `ADMIN` - Accès complet à la gestion des utilisateurs
- `USER` - Accès aux fonctionnalités de base

**Base de données:** PostgreSQL (authdb)

### 4. Docteur Service (Port 8081)
**Gestion des Docteurs**

**Endpoints:**
- `GET /api/docteurs` - Liste tous les docteurs (public)
- `GET /api/docteurs/{id}` - Détails d'un docteur (public)
- `POST /api/docteurs` - Créer un docteur (Admin)
- `PUT /api/docteurs/{id}` - Modifier un docteur (Admin)
- `DELETE /api/docteurs/{id}` - Supprimer un docteur (Admin)

**Base de données:** PostgreSQL (docteurdb)

**Données pré-chargées:**
- Dr. Alami Ahmed - Cardiologie
- Dr. Bennani Fatima - Pédiatrie
- Dr. Cohen David - Dermatologie
- Dr. Douiri Sanaa - Gynécologie
- Dr. El Amrani Karim - Neurologie
- Dr. Fassi Layla - Ophtalmologie

### 5. RDV Service (Port 8082)
**Gestion des Rendez-vous**

**Endpoints:**
- `GET /api/rdv` - Liste tous les rendez-vous (public)
- `GET /api/rdv/{id}` - Détails d'un rendez-vous (public)
- `GET /api/rdv/docteur/{docteurId}` - Rendez-vous par docteur
- `POST /api/rdv` - Créer un rendez-vous (public)
- `PUT /api/rdv/{id}` - Modifier un rendez-vous
- `DELETE /api/rdv/{id}` - Annuler un rendez-vous

**Fonctionnalités:**
- Validation de l'existence du docteur via Feign Client
- Publication d'événements dans RabbitMQ lors de créations/modifications
- Circuit Breaker pour la communication avec Docteur Service
- Validation des données (date future, champs obligatoires)

**Base de données:** PostgreSQL (rdvdb)

### 6. Notification Service (Port 8083)
**Envoi de Notifications Asynchrones**

**Fonctionnalités:**
- Écoute des événements RabbitMQ (création, modification, annulation de RDV)
- Écoute des événements de facturation (création de facture, confirmation de paiement)
- Envoi d'emails via Resend API
- Support SMS (simulé)
- Gestion des erreurs et retry automatique

**Types de notifications:**
- Confirmation de création de rendez-vous
- Rappel de modification
- Confirmation d'annulation
- Facture générée
- Confirmation de paiement

**Intégration:** Resend (emails uniquement en développement)

### 7. Billing Service (Port 8085)
**Gestion de la Facturation et des Paiements**

**Endpoints:**
- `GET /api/billing/invoices` - Liste toutes les factures
- `GET /api/billing/invoices/{id}` - Détails d'une facture
- `GET /api/billing/invoices/patient/{email}` - Factures par patient
- `GET /api/billing/invoices/status/{status}` - Factures par statut
- `POST /api/billing/invoices` - Créer une facture manuellement
- `PUT /api/billing/invoices/{id}` - Modifier une facture
- `GET /api/billing/payments/invoice/{invoiceId}` - Paiements d'une facture
- `POST /api/billing/payments` - Enregistrer un paiement
- `GET /api/billing/outstanding/{email}` - Solde impayé par patient

**Fonctionnalités:**
- Génération automatique de factures lors de la création de rendez-vous
- Gestion des paiements avec plusieurs méthodes (Cash, Card, Bank Transfer, Online)
- Suivi du statut des factures (PENDING, PAID, PARTIALLY_PAID, OVERDUE, CANCELLED)
- Calcul du solde impayé par patient
- Publication d'événements dans RabbitMQ pour notifications
- Prix par défaut : 300.00 MAD par consultation

**Base de données:** PostgreSQL (billingdb)

**Tables:**
- Invoice - Factures des consultations
- Payment - Enregistrements des paiements
- Pricing - Tarifs par spécialité

## Installation et Démarrage

### Prérequis
- **Java 17** ou supérieur
- **Maven 3.6** ou supérieur
- **Node.js 16** ou supérieur
- **PostgreSQL 12** ou supérieur
- **Docker** (pour RabbitMQ)

### 1. Configuration des Bases de Données

```bash
# Démarrer PostgreSQL et créer les bases
psql -U postgres

CREATE DATABASE authdb;
CREATE DATABASE docteurdb;
CREATE DATABASE rdvdb;
CREATE DATABASE billingdb;
```

Ou utiliser le script fourni:
```bash
chmod +x setup-databases.sh
./setup-databases.sh
```

### 2. Démarrer RabbitMQ

```bash
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

Console RabbitMQ : http://localhost:15672 (guest/guest)

### 3. Démarrer les Services Backend

**Option A: Démarrage manuel**

```bash
# Terminal 1 - Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2 - API Gateway
cd api-gateway
mvn spring-boot:run

# Terminal 3 - Auth Service
cd auth-service
mvn spring-boot:run

# Terminal 4 - Docteur Service
cd docteur-service
mvn spring-boot:run

# Terminal 5 - RDV Service
cd rdv-service
mvn spring-boot:run

# Terminal 6 - Notification Service
cd notification-service
mvn spring-boot:run

# Terminal 7 - Billing Service
cd billing-service
mvn spring-boot:run
```

**Option B: Script automatique**

```bash
chmod +x start-all.sh
./start-all.sh
```

### 4. Démarrer le Frontend React

```bash
cd frontend
npm install
npm start
```

L'application sera disponible sur http://localhost:3000

### 5. Vérification

- ✅ Eureka Dashboard : http://localhost:8761
- ✅ API Gateway Health : http://localhost:8080/actuator/health
- ✅ RabbitMQ Console : http://localhost:15672
- ✅ Frontend : http://localhost:3000

## Utilisation

### Accès Public (Sans Authentification)

1. Ouvrir http://localhost:3000
2. Consulter la liste des docteurs
3. Créer un rendez-vous
4. Consulter tous les rendez-vous

### Accès Administrateur

**Compte Admin par défaut:**
- Username: `admin`
- Password: `admin123`

**Fonctionnalités Admin:**
1. Se connecter via le panneau d'authentification
2. Gérer les utilisateurs (créer, modifier, supprimer)
3. Gérer les docteurs (créer, modifier, supprimer)
4. Consulter et gérer les factures
5. Enregistrer les paiements
6. Voir les soldes impayés par patient

## Structure du Projet

```
microservices-medical-appointment/
├── eureka-server/              # Service Discovery
├── api-gateway/                # API Gateway avec JWT
├── auth-service/               # Authentification JWT
│   ├── entities/User.java
│   ├── services/AuthService.java
│   ├── utils/JwtUtil.java
│   └── config/SecurityConfig.java
├── docteur-service/            # Gestion des docteurs
│   ├── entities/Docteur.java
│   ├── repositories/DocteurRepository.java
│   └── controllers/DocteurController.java
├── rdv-service/                # Gestion des rendez-vous
│   ├── entities/Rdv.java
│   ├── controllers/RdvController.java
│   ├── clients/DocteurClient.java (Feign)
│   ├── events/AppointmentEventPublisher.java
│   └── config/RabbitMQConfig.java
├── notification-service/       # Notifications asynchrones
│   ├── listeners/AppointmentEventListener.java
│   ├── listeners/PaymentEventListener.java
│   ├── services/NotificationService.java
│   └── config/RabbitMQConfig.java
├── billing-service/            # Gestion de la facturation
│   ├── entities/
│   │   ├── Invoice.java
│   │   ├── Payment.java
│   │   └── Pricing.java
│   ├── repositories/
│   │   ├── InvoiceRepository.java
│   │   ├── PaymentRepository.java
│   │   └── PricingRepository.java
│   ├── services/BillingService.java
│   ├── controllers/BillingController.java
│   ├── listeners/AppointmentEventListener.java
│   └── config/RabbitMQConfig.java
├── frontend/                   # Application React
│   ├── components/
│   │   ├── AuthPanel.js
│   │   ├── DocteurList.js
│   │   ├── DocteurManagement.js
│   │   ├── RdvForm.js
│   │   ├── RdvList.js
│   │   ├── UserManagement.js
│   │   └── InvoiceManagement.js
│   └── services/
│       ├── apiClient.js
│       ├── auth.js
│       └── api.js
├── diagrams/                   # Diagrammes UML PlantUML
└── docs/                       # Documentation
```

## Patterns et Concepts Implémentés

### 1. Architecture Microservices
- Services indépendants et déployables séparément
- Base de données par service
- Communication via API REST et messaging

### 2. API Gateway Pattern
- Point d'entrée unique pour tous les clients
- Routage intelligent vers les services backend
- Gestion centralisée de la sécurité et CORS

### 3. Service Discovery
- Enregistrement automatique des services
- Load balancing côté client
- Health checking

### 4. Circuit Breaker Pattern
- Protection contre les défaillances en cascade
- Fallback methods
- Configuration Resilience4j

### 5. Event-Driven Architecture
- Publication d'événements dans RabbitMQ
- Consommation asynchrone par Notification Service et Billing Service
- Découplage entre services
- Support pour plusieurs consommateurs d'événements

### 6. Saga Pattern (Orchestration)
- Gestion des transactions distribuées
- Coordination entre RDV, Billing et Notification services
- Compensation automatique en cas d'échec

### 7. Security Patterns
- JWT pour l'authentification stateless
- Role-Based Access Control (RBAC)
- Endpoints publics configurables

### 8. Exception Handling
- GlobalExceptionHandler pour gestion centralisée
- Validation des inputs
- Messages d'erreur cohérents

## Configuration

### Variables d'Environnement Importantes

**Auth Service:**
```properties
jwt.secret=your-secret-key-min-256-bits
jwt.expiration=86400000
```

**Notification Service:**
```properties
resend.api.key=your-resend-api-key
resend.from.email=your-verified-email@domain.com
```

**Billing Service:**
```properties
billing.default.consultation.fee=300.00
billing.tax.rate=0.0
billing.payment.term.days=30
```

**RabbitMQ (tous les services):**
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## Scripts Utilitaires

- `start-all.sh` - Démarre tous les services
- `stop-all.sh` - Arrête tous les services
- `setup-databases.sh` - Crée les bases PostgreSQL
- `reset-auth-db.sh` - Réinitialise la base authdb
- `check-health.sh` - Vérifie le statut de tous les services

## Monitoring et Logs

- Logs centralisés dans `/tmp/*-service.log`
- Actuator endpoints sur tous les services
- Health checks via `/actuator/health`
- Eureka Dashboard pour le statut des services

## Tests

Données de test pré-chargées :
- 6 docteurs avec spécialités variées
- Utilisateur admin (admin/admin123)
- Utilisateur test (user/user)
- Prix par défaut : 300.00 MAD par consultation

## Documentation

- **Diagrammes UML** : Disponibles dans `/diagrams`
- **Guides de déploiement** : Dans `/docs`
- **QUICK_START.md** : Guide rapide de démarrage
- **SETUP_INSTRUCTIONS.md** : Instructions détaillées
- **BILLING_SERVICE_IMPLEMENTATION.md** : Guide complet du service de facturation

## Screenshots

Des captures d'écran de l'application sont disponibles dans le répertoire `/screenshots` :
- Interface d'administration
- Gestion des docteurs
- Formulaire de prise de rendez-vous
- Système de facturation
- Notifications par email

## Évolutions Futures

### Court Terme
- 🔄 Intégration de passerelles de paiement (Stripe, PayPal)
- 📊 Tableau de bord analytique pour les administrateurs
- 📧 Génération de factures PDF
- 📱 Application mobile (React Native)

### Moyen Terme
- 🔐 Authentification à deux facteurs (2FA)
- 🌍 Support multilingue (Français, Arabe, Anglais)
- 📅 Rappels automatiques de rendez-vous
- 💳 Plans de paiement échelonné

### Long Terme
- 🤖 Assistant virtuel avec IA
- 📈 Rapports financiers avancés
- 🔗 Intégration avec systèmes hospitaliers
- 🎯 Système de fidélité patients

## Contributing

Les contributions sont les bienvenues ! Pour contribuer :

1. **Fork** le projet
2. **Créez** votre branche feature (`git checkout -b feature/AmazingFeature`)
3. **Committez** vos changements (`git commit -m 'Add some AmazingFeature'`)
4. **Push** vers la branche (`git push origin feature/AmazingFeature`)
5. **Ouvrez** une Pull Request

### Règles de Contribution
- Suivre les conventions de code existantes
- Ajouter des tests pour les nouvelles fonctionnalités
- Mettre à jour la documentation si nécessaire
- S'assurer que tous les tests passent avant de soumettre

## Auteur

**Sohaib Mokhliss**

Projet réalisé dans le cadre du cours d'Architecture des Composants - Microservices avec Spring Boot et React.

## Remerciements

- Spring Boot et Spring Cloud pour le framework microservices
- React pour l'interface utilisateur moderne
- RabbitMQ pour le messaging asynchrone
- PostgreSQL pour la persistance des données
- La communauté open source pour les nombreuses bibliothèques utilisées

---

**Dernière mise à jour :** Décembre 2025

## Licence

Ce projet est à usage éducatif uniquement.
