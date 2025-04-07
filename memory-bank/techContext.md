# WorkforceHub Technical Context

## Development Environment

### Required Tools
- Node.js (v18 or higher)
- Java JDK 17
- MySQL 8.0
- Git
- VS Code or IntelliJ IDEA
- Postman for API testing

### IDE Extensions
- ESLint
- Prettier
- Tailwind CSS IntelliSense
- Spring Boot Tools
- Lombok
- GitLens

## Project Structure

### Frontend Structure
```
frontend-web/
├── frontend-website/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── utils/
│   │   ├── types/
│   │   ├── services/
│   │   └── assets/
│   ├── public/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   └── tailwind.config.js
```

### Backend Structure
```
backend/
├── workforce/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── workforce/
│   │   │   │           ├── config/
│   │   │   │           ├── controller/
│   │   │   │           ├── model/
│   │   │   │           ├── repository/
│   │   │   │           ├── service/
│   │   │   │           └── util/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   └── application.properties
```

## Dependencies

### Frontend Dependencies
```json
{
  "dependencies": {
    "@headlessui/react": "^1.7.18",
    "@heroicons/react": "^2.1.1",
    "@hookform/resolvers": "^3.3.4",
    "@microsoft/mgt-react": "^3.0.0",
    "@microsoft/mgt-spfx": "^3.0.0",
    "axios": "^1.6.7",
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-hook-form": "^7.50.1",
    "react-router-dom": "^6.22.1",
    "yup": "^1.3.3"
  },
  "devDependencies": {
    "@types/react": "^18.2.55",
    "@types/react-dom": "^18.2.19",
    "@typescript-eslint/eslint-plugin": "^6.21.0",
    "@typescript-eslint/parser": "^6.21.0",
    "@vitejs/plugin-react": "^4.2.1",
    "autoprefixer": "^10.4.17",
    "eslint": "^8.56.0",
    "postcss": "^8.4.35",
    "tailwindcss": "^3.4.1",
    "typescript": "^5.2.2",
    "vite": "^5.1.0"
  }
}
```

### Backend Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

## Configuration

### Frontend Configuration
```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

### Backend Configuration
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/workforce
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

jwt.secret=your-secret-key
jwt.expiration=86400000

server.port=8080
```

## Development Workflow

### 1. Setup
1. Clone the repository
2. Install frontend dependencies: `npm install`
3. Install backend dependencies: `mvn install`
4. Configure environment variables
5. Start MySQL database

### 2. Development
1. Start backend server: `mvn spring-boot:run`
2. Start frontend dev server: `npm run dev`
3. Access application at `http://localhost:3000`

### 3. Testing
1. Run frontend tests: `npm test`
2. Run backend tests: `mvn test`
3. Run E2E tests: `npm run cypress:open`

### 4. Building
1. Build frontend: `npm run build`
2. Build backend: `mvn clean package`
3. Run application: `java -jar target/workforce-0.0.1-SNAPSHOT.jar`

## Deployment

### Frontend Deployment
1. Build the application
2. Deploy to static hosting (e.g., Netlify, Vercel)
3. Configure environment variables
4. Set up CI/CD pipeline

### Backend Deployment
1. Build the application
2. Deploy to cloud platform (e.g., AWS, Azure)
3. Configure database connection
4. Set up monitoring and logging

## Monitoring and Logging

### Frontend
- Error tracking with Sentry
- Performance monitoring
- User analytics
- Console logging

### Backend
- Application metrics
- Database monitoring
- Security logging
- Error tracking 