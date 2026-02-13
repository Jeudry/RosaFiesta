# Database Schema

This document represents the current state of the PostgreSQL database schema.

## Entity Relationship Diagram

```mermaid
erDiagram
    USERS ||--o{ POSTS : "creates"
    USERS ||--o{ COMMENTS : "writes"
    USERS ||--o{ REFRESH_TOKENS : "has"
    ROLES ||--o{ USERS : "assigned to"
    
    POSTS ||--o{ COMMENTS : "has"
    POSTS }|--|{ TAGS : "tagged with (conceptual)"
    
    CATEGORIES ||--o{ CATEGORIES : "parent of"
    CATEGORIES ||--o{ ARTICLES : "contains"
    
    ARTICLES ||--o{ ARTICLE_VARIANTS : "has variants"
    ARTICLE_VARIANTS ||--o{ ARTICLE_VARIANT_ATTRIBUTES : "has attributes"
    ARTICLE_VARIANTS ||--|| ARTICLE_VARIANT_DIMENSIONS : "has dimensions"

    USERS {
        uuid id PK
        varchar user_name
        varchar first_name
        varchar last_name
        citext email
        bytea password
        uuid role_id FK
        boolean is_active
        timestamp created_at
    }

    ROLES {
        uuid id PK
        varchar name
        int level
        text description
    }

    POSTS {
        uuid id PK
        varchar title
        varchar content
        uuid user_id FK
        text[] tags
        int version
        timestamp created_at
    }

    COMMENTS {
        uuid id PK
        uuid post_id FK
        uuid user_id FK
        text content
    }

    CATEGORIES {
        uuid id PK
        varchar name
        uuid parent_id FK
        text image_url
    }

    ARTICLES {
        uuid id PK
        varchar name_template
        varchar description_template
        uuid category_id FK
        varchar type
        boolean is_active
    }

    ARTICLE_VARIANTS {
        uuid id PK
        uuid article_id FK
        varchar sku
        varchar name
        decimal rental_price
        decimal sale_price
        int stock
    }

    ARTICLE_VARIANT_ATTRIBUTES {
        uuid variant_id PK, FK
        varchar key PK
        varchar value
    }

    ARTICLE_VARIANT_DIMENSIONS {
        uuid id PK
        uuid variant_id FK
        decimal height
        decimal width
        decimal depth
        decimal weight
    }

    REFRESH_TOKENS {
        uuid id PK
        uuid user_id FK
        varchar token
        timestamp expires_at
    }
```

## Tables Detail

### Users & Auth
- **users**: Core user identity. `email` is case-insensitive (CITEXT). `role_id` links to RBAC.
- **roles**: RBAC roles (Admin, Moderator, User).
- **refresh_tokens**: Long-lived JWT refresh tokens.

### Social
- **posts**: User content. Supports optimistic locking via `version`.
- **comments**: Responses to posts.

### Catalog (E-commerce)
- **categories**: Hierarchical category tree (Adjacency List pattern via `parent_id`).
- **articles**: Base product definition (e.g., "T-Shirt"). Holds shared data like Description Template.
- **article_variants**: Sellable SKUs (e.g., "Red T-Shirt Size L"). Holds Prices and Inventory.
- **article_variant_attributes**: EAV-lite for variant specifics (Color=Red, Size=L).
- **article_variant_dimensions**: Physical dimensions for shipping/logistics.
