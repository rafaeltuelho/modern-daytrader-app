# DayTrader Augment CLI Subagents

This directory contains specialized subagent definitions for the DayTrader modernization project. These agents are configured to work with the Augment CLI and follow a **specification-driven development approach** where architectural specifications guide all implementation work.

## 🎯 Spec-Driven Development Approach

**All agents now follow a specification-driven workflow:**

1. **Java Architect** creates detailed architectural specifications in discrete phases
2. **All other agents** read and reference these specifications before beginning work
3. **Specifications are stored** in the `/specs` folder with a clear index and phase structure
4. **Human-in-the-loop review** ensures user approval before implementation begins
5. **Deviations are tracked** and require specification updates

### Specification Workflow

```
Java Architect
    ↓
Creates Phase Specifications (Phase 1, 2, 3, 4...)
    ↓
Presents to User for Review & Approval
    ↓
User Reviews, Asks Questions, Approves
    ↓
API Designer, Quarkus Engineer, Frontend Engineer, QA Engineer
    ↓
Read Specifications → Implement → Reference Specs → Flag Deviations
```

## Available Agents

### 1. **Java Architect** (`java-architect.md`)
- **Model**: opus4.5 (advanced reasoning)
- **Color**: Orange
- **Specialization**: Application architecture, design patterns, migration strategy
- **Primary Output**: Structured architectural specifications in discrete phases
- **Key Workflow**:
  - Creates Phase 1, 2, 3, 4... specifications
  - Each phase includes: objectives, technical approach, dependencies, acceptance criteria, scope
  - **PAUSES after drafting** to present plan to user for review
  - Documents user feedback and decisions in specifications
  - **Does NOT proceed** until user explicitly approves
- **Use Cases**:
  - Create Phase specifications for modernization
  - Design cloud-native architectures
  - Plan phased modernization strategies
  - Evaluate technology choices
  - Design package structures and module boundaries

### 2. **API Designer** (`api-designer.md`)
- **Model**: sonnet4.5 (balanced)
- **Color**: Blue
- **Specialization**: REST/GraphQL API design, OpenAPI specifications
- **Key Workflow**:
  - Reads relevant phase specifications from `/specs` folder
  - Validates API design aligns with architectural specifications
  - References specifications in API design documents
  - Flags any deviations from specifications
- **Use Cases**:
  - Design RESTful endpoints per specifications
  - Generate OpenAPI/Swagger specifications
  - Create API documentation
  - Design error handling strategies
  - Plan API versioning and evolution

### 3. **Quarkus Engineer** (`quarkus-engineer.md`)
- **Model**: sonnet4.5 (balanced)
- **Color**: Red
- **Specialization**: Quarkus framework, reactive programming, CDI
- **Key Workflow**:
  - Reads relevant phase specifications from `/specs` folder
  - Validates implementation aligns with architectural and API specifications
  - References specifications in code and commit messages
  - Flags any deviations from specifications
  - Uses implementation checklist to ensure spec alignment
- **Use Cases**:
  - Implement Quarkus services per specifications
  - Configure Quarkus extensions
  - Write CDI-managed business services
  - Implement Hibernate ORM with Panache
  - Write unit and integration tests

### 4. **Frontend Engineer** (`frontend-engineer.md`)
- **Model**: sonnet4.5 (balanced)
- **Color**: Purple
- **Specialization**: Frontend app development, UI/UX design, modern JavaScript/CSS frameworks
- **Key Workflow**:
  - Reads relevant phase specifications from `/specs` folder
  - Reviews API specifications to understand backend contracts
  - Validates frontend implementation aligns with architectural specifications
  - References specifications in component code and documentation
  - Flags any deviations from specifications
  - Uses implementation checklist to ensure spec alignment
- **Use Cases**:
  - Design React component architecture per specifications
  - Implement responsive UI with Tailwind CSS
  - Integrate with backend APIs following API specifications
  - Implement authentication and JWT token management
  - Create accessible, performant user interfaces
  - Write component tests and E2E tests

### 5. **QA Engineer** (`qa-engineer.md`)
- **Model**: sonnet4.5 (balanced)
- **Color**: Green
- **Specialization**: Testing strategy, test automation, quality assurance
- **Key Workflow**:
  - Reads relevant phase specifications from `/specs` folder
  - Identifies acceptance criteria from specifications
  - Maps acceptance criteria to test cases
  - References specifications in test code and documentation
  - Flags any specification gaps or issues discovered during testing
- **Use Cases**:
  - Design test strategies aligned with specifications
  - Write unit and integration tests
  - Migrate legacy tests to modern frameworks
  - Measure and improve code coverage
  - Set up test automation in CI/CD

## How to Use

### Specification-Driven Workflow

**Step 1: Create Specifications**
```bash
auggie
> Use the java-architect agent to create Phase 1 specifications for core infrastructure
(Agent creates detailed specs and presents them for review)
```

**Step 2: Review & Approve**
- Review the specifications presented by the architect
- Ask clarifying questions
- Request explanations of trade-offs
- Approve or request revisions

**Step 3: Implement Per Specifications**
```bash
> Use the api-designer agent to design REST endpoints per Phase 1 specification
> Use the quarkus-engineer agent to implement services per Phase 1 specification
> Use the frontend-engineer agent to design React components per Phase 1 specification
> Use the qa-engineer agent to write tests per Phase 1 specification
```

### In Augment CLI Interactive Mode

```bash
# Start interactive mode
auggie

# Create specifications
> Use the java-architect agent to create Phase 1 specifications for core infrastructure

# (Review and approve specifications)

# Implement per specifications
> Use the api-designer agent to design REST endpoints per Phase 1 spec
> Use the quarkus-engineer agent to implement services per Phase 1 spec
> Use the frontend-engineer agent to design React components per Phase 1 spec
> Use the qa-engineer agent to write tests per Phase 1 spec
```

### In Your IDE

When using Augment in your IDE (VS Code, JetBrains), you can reference agents:

```
@java-architect Create Phase 1 specifications for core infrastructure and database layer
(Review and approve)

@api-designer Design REST endpoints per Phase 1 specification
@quarkus-engineer Implement services per Phase 1 specification
@frontend-engineer Design React components per Phase 1 specification
@qa-engineer Write tests per Phase 1 specification
```

## Specification Structure

Specifications are stored in `/specs` folder with the following structure:

```
/specs/
├── spec-index.md                      # Master index of all phases and status
├── phase-01-core-infrastructure.md    # Phase 1 specification
├── phase-02-feature-implementation.md # Phase 2 specification
├── phase-03-integration-optimization.md # Phase 3 specification
└── phase-04-testing-deployment.md     # Phase 4 specification
```

Each phase specification includes:
- **Objectives**: Clear goals for the phase
- **Technical Approach**: Detailed design decisions and patterns
- **Dependencies**: Prerequisites and external dependencies
- **Acceptance Criteria**: Measurable success criteria
- **Estimated Scope**: Story points, effort estimates, timeline
- **Risks & Mitigations**: Identified risks and mitigation strategies
- **Implementation Notes**: Specific guidance for implementation teams

## DayTrader Context

All agents are configured with context about the DayTrader modernization:

- **Legacy Stack**: Java EE7 + JSF 2.2 + EJB3 + WebSphere Liberty + Derby
- **Target Stack**: Quarkus 3.x + React 18 + PostgreSQL + JWT
- **Key Patterns**: CDI services, Panache ORM, RESTEasy Reactive, SmallRye JWT
- **API Structure**: `/api/auth`, `/api/account`, `/api/trade`, `/api/market`, etc.

## Agent Collaboration

These agents work together in a specification-driven workflow:

1. **Java Architect** creates Phase specifications and presents them for user review
2. **User** reviews specifications, asks questions, and approves
3. **API Designer** reads Phase specifications and designs REST APIs
4. **Quarkus Engineer** reads Phase specifications and implements services
5. **Frontend Engineer** reads Phase specifications and API specs, designs React components
6. **QA Engineer** reads Phase specifications and creates test strategies
7. **All agents** reference specifications and flag deviations

## Tips for Best Results

- **Start with specifications**: Always begin with the Java Architect creating Phase specifications
- **Review before implementation**: Review and approve specifications before implementation begins
- **Reference specifications**: Ask agents to reference relevant Phase specifications in their work
- **Flag deviations**: Ask agents to flag any deviations from specifications and propose updates
- **Use agents in sequence**: Follow the specification-driven workflow for best results
- **Document decisions**: Keep specifications updated with decisions and trade-offs discussed

