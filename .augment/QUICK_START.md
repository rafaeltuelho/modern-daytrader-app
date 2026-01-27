# Quick Start: Spec-Driven Development with Augment Agents

## 5-Minute Overview

The DayTrader Augment agents follow a **specification-driven workflow**:

1. **Java Architect** creates detailed Phase specifications
2. **You review** and approve the specifications
3. **Other agents** implement per the approved specifications
4. **All work** references and aligns with specifications

## Getting Started

### Step 1: Create Phase 1 Specifications

```bash
auggie
> Use the java-architect agent to create Phase 1 specifications for core infrastructure
```

The architect will:
- Create detailed Phase 1 specification
- Write it to `/specs/phase-01-core-infrastructure.md`
- **PAUSE and present** the plan to you for review

### Step 2: Review & Approve

Read the specification and:
- ✅ Ask clarifying questions
- ✅ Request explanations of trade-offs
- ✅ Suggest modifications
- ✅ Approve or request revisions

### Step 3: Implement Per Specifications

Once approved, use other agents:

```bash
> Use the api-designer agent to design REST endpoints per Phase 1 specification
> Use the quarkus-engineer agent to implement services per Phase 1 specification
> Use the qa-engineer agent to write tests per Phase 1 specification
```

## Key Files

| File | Purpose |
|------|---------|
| `agents/java-architect.md` | Architect agent definition |
| `agents/api-designer.md` | API Designer agent definition |
| `agents/quarkus-engineer.md` | Quarkus Engineer agent definition |
| `agents/qa-engineer.md` | QA Engineer agent definition |
| `agents/README.md` | Agent overview and usage guide |
| `SPEC_DRIVEN_DEVELOPMENT.md` | Detailed workflow guide |
| `PHASE_SPECIFICATION_TEMPLATE.md` | Template for phase specifications |

## Specification Folder Structure

```
/specs/
├── spec-index.md                      # Master index
├── phase-01-core-infrastructure.md    # Phase 1 spec
├── phase-02-feature-implementation.md # Phase 2 spec
├── phase-03-integration-optimization.md # Phase 3 spec
└── phase-04-testing-deployment.md     # Phase 4 spec
```

## What Each Agent Does

### Java Architect
- Creates Phase specifications
- Presents to you for review
- **PAUSES until you approve**
- Documents decisions and trade-offs

### API Designer
- Reads Phase specifications
- Designs REST APIs per spec
- References specifications in design
- Flags deviations from spec

### Quarkus Engineer
- Reads Phase specifications
- Implements services per spec
- References specifications in code
- Flags deviations from spec

### QA Engineer
- Reads Phase specifications
- Creates tests for acceptance criteria
- References specifications in tests
- Flags specification gaps

## Common Commands

### Create Specifications
```
@java-architect Create Phase 1 specifications for core infrastructure
```

### Design APIs
```
@api-designer Design REST endpoints per Phase 1 specification
```

### Implement Services
```
@quarkus-engineer Implement services per Phase 1 specification
```

### Write Tests
```
@qa-engineer Write tests per Phase 1 specification
```

## Important Principles

1. **Specifications First** - Always create specs before implementation
2. **User Approval** - User reviews and approves all specs
3. **Reference Specs** - All implementation references approved specs
4. **Flag Deviations** - Any deviations from specs are documented
5. **Continuous Alignment** - All agents validate alignment with specs

## Handling Deviations

If implementation reveals a need to deviate from specifications:

1. Document why the deviation is necessary
2. Propose a specification update
3. Wait for user approval
4. Update the specification
5. Proceed with implementation

## Need Help?

- **Workflow Guide**: See `SPEC_DRIVEN_DEVELOPMENT.md`
- **Agent Details**: See `agents/README.md`
- **Specification Template**: See `PHASE_SPECIFICATION_TEMPLATE.md`

## Next Steps

1. Read `agents/README.md` for agent overview
2. Use Java Architect to create Phase 1 specifications
3. Review and approve specifications
4. Use other agents to implement per specifications
5. Reference specifications in all implementation work

