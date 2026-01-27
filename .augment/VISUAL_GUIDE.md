# Visual Guide: Spec-Driven Development Workflow

## The Workflow at a Glance

```
┌─────────────────────────────────────────────────────────────────┐
│                    SPECIFICATION CREATION                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  @java-architect Create Phase 1 specifications                   │
│         ↓                                                         │
│  Architect creates detailed Phase 1 spec                         │
│         ↓                                                         │
│  Architect PAUSES and presents to user                           │
│         ↓                                                         │
│  ⏸️  WAITING FOR USER REVIEW & APPROVAL                          │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    USER REVIEW & APPROVAL                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  User reads specification                                        │
│  User asks clarifying questions                                  │
│  User requests explanations of trade-offs                        │
│  User suggests modifications                                     │
│  User APPROVES or requests revisions                             │
│         ↓                                                         │
│  ✅ APPROVED - Proceed to implementation                         │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    IMPLEMENTATION PHASE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  @api-designer Design REST endpoints per Phase 1 spec            │
│  @quarkus-engineer Implement services per Phase 1 spec           │
│  @qa-engineer Write tests per Phase 1 specification              │
│         ↓                                                         │
│  All agents:                                                      │
│  ✓ Read Phase 1 specification                                    │
│  ✓ Validate alignment with spec                                  │
│  ✓ Reference spec in their work                                  │
│  ✓ Flag any deviations from spec                                 │
│         ↓                                                         │
│  ✅ IMPLEMENTATION COMPLETE                                      │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Agent Responsibilities

```
┌──────────────────────────────────────────────────────────────────┐
│                    JAVA ARCHITECT                                 │
├──────────────────────────────────────────────────────────────────┤
│ Input:  Modernization goals and requirements                      │
│ Output: Phase specifications (Phase 1, 2, 3, 4...)               │
│ Process:                                                          │
│   1. Create detailed phase specifications                         │
│   2. Write to /specs/phase-NN-<description>.md                   │
│   3. PAUSE and present to user                                   │
│   4. Document user feedback and decisions                         │
│   5. Wait for explicit approval before proceeding                │
│ Key: HUMAN-IN-THE-LOOP REVIEW REQUIRED                           │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│                    API DESIGNER                                   │
├──────────────────────────────────────────────────────────────────┤
│ Input:  Phase specifications from /specs folder                  │
│ Output: REST API designs and OpenAPI specifications              │
│ Process:                                                          │
│   1. Read relevant phase specification                            │
│   2. Validate alignment with architectural decisions              │
│   3. Design APIs per approved specification                       │
│   4. Reference specification in design documents                  │
│   5. Flag any deviations and propose updates                      │
│ Key: SPECIFICATION-DRIVEN DESIGN                                 │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│                    QUARKUS ENGINEER                               │
├──────────────────────────────────────────────────────────────────┤
│ Input:  Phase specifications and API designs                     │
│ Output: Implemented Quarkus services and endpoints               │
│ Process:                                                          │
│   1. Read relevant phase specification                            │
│   2. Validate alignment with architectural and API specs          │
│   3. Implement services per approved specification                │
│   4. Reference specification in code and commits                  │
│   5. Flag any deviations and propose updates                      │
│ Key: SPECIFICATION-DRIVEN IMPLEMENTATION                         │
└──────────────────────────────────────────────────────────────────┘
                              ↓
┌──────────────────────────────────────────────────────────────────┐
│                    QA ENGINEER                                    │
├──────────────────────────────────────────────────────────────────┤
│ Input:  Phase specifications and implemented code                │
│ Output: Comprehensive test suite                                 │
│ Process:                                                          │
│   1. Read relevant phase specification                            │
│   2. Identify acceptance criteria from specification              │
│   3. Map criteria to test cases                                   │
│   4. Create tests per approved test strategy                      │
│   5. Flag specification gaps or issues discovered                 │
│ Key: SPECIFICATION-DRIVEN TEST STRATEGY                          │
└──────────────────────────────────────────────────────────────────┘
```

## Specification Structure

```
/specs/
│
├── spec-index.md
│   └── Master index of all phases and status
│
├── phase-01-core-infrastructure.md
│   ├── Objectives
│   ├── Technical Approach
│   ├── Dependencies
│   ├── Acceptance Criteria
│   ├── Estimated Scope
│   ├── Risks & Mitigations
│   ├── Implementation Notes
│   └── User Feedback & Decisions
│
├── phase-02-feature-implementation.md
│   └── [Same structure as Phase 1]
│
├── phase-03-integration-optimization.md
│   └── [Same structure as Phase 1]
│
└── phase-04-testing-deployment.md
    └── [Same structure as Phase 1]
```

## Key Principles

```
┌─────────────────────────────────────────────────────────────────┐
│  1. SPECIFICATIONS FIRST                                         │
│     Create and approve specifications before implementation      │
│                                                                   │
│  2. HUMAN-IN-THE-LOOP                                            │
│     User reviews and approves all architectural decisions        │
│                                                                   │
│  3. TRACEABILITY                                                 │
│     All implementation references approved specifications        │
│                                                                   │
│  4. DEVIATION TRACKING                                           │
│     Deviations are documented and require approval               │
│                                                                   │
│  5. CONTINUOUS ALIGNMENT                                         │
│     All agents validate alignment with specifications            │
└─────────────────────────────────────────────────────────────────┘
```

## Handling Deviations

```
Implementation reveals need to deviate from specification
                    ↓
Document the deviation clearly
                    ↓
Explain why deviation is necessary
                    ↓
Propose specification update
                    ↓
⏸️  WAIT FOR USER APPROVAL
                    ↓
User approves deviation
                    ↓
Update specification document
                    ↓
Proceed with implementation
```

## Documentation Files

```
.augment/
├── agents/
│   ├── README.md                    ← Start here for overview
│   ├── java-architect.md            ← Architect agent definition
│   ├── api-designer.md              ← API Designer agent definition
│   ├── quarkus-engineer.md          ← Quarkus Engineer agent definition
│   └── qa-engineer.md               ← QA Engineer agent definition
│
├── QUICK_START.md                   ← 5-minute overview
├── SPEC_DRIVEN_DEVELOPMENT.md       ← Detailed workflow guide
├── PHASE_SPECIFICATION_TEMPLATE.md  ← Template for creating specs
└── VISUAL_GUIDE.md                  ← This file
```

## Getting Started

1. Read `QUICK_START.md` (5 minutes)
2. Read `agents/README.md` (10 minutes)
3. Use Java Architect to create Phase 1 specifications
4. Review and approve specifications
5. Use other agents to implement per specifications

