## Agent Prompts used to refine the Modernization Agents.

### Original Prompt

```
Analyze the Agent definitions in the `.augment/agents/`. I want them to work in a coordinated way. Starting with the software-architect agent, each agent passes down specs/plan to the next agent in this order:

1. software-architect
2. software-engineer
3. verifier (needs to be defined)
4. qa-engineer (focused on banked test coverage)
5. frontend-engineer
6. verifier (needs to be defined)
7. qa-engineer (focused on frontend test coverage)

The verifier agent needs to be defined. His primary job is to verify the previous agent (software-engineer or frontend-engineer) outcome against the initial specs/plan produced by the software-architect (stored in the /spec folder)

I also want to revisit these agents' definitions:
 - I want to merge the api-designer into the software-achitect or into the software-engineer. What is your take here? I want to hear your opinion about which role is most appropriate for API designing.
 - whenever possible and needed the software-engineer and the frontend-engineer should use the Context 7 MCP tools to obtain updated framework/library documentation/guides. If not possible to use Context 7 use the built-in web_search tool for tthat.
 - Whenever possible, I want the qa-engineer to leverage the Chrome devtools MCP to test out the web frontend (page navigations and main workflows). 
 - The qa-engineer needs to loop through the test runs until all test cases pass.

All agents must use the specs/ folder to store their outputs and notes produced during their runs. The main form of communication between these agents is spec/plan (markdown files) stored in the specs/ folder.

KEEP IN MIND that these Agents will be used to in an App Modernization use-case. So the  software-architect must have this in mind while performing the initial analyzis and producing the specs/plan. The other agents will perform the app modernization work based on his outcome. 
```

### 1st Enhanced prompt

```
I need to redesign the agent workflow in `.augment/agents/` to create a coordinated, sequential pipeline where agents communicate through specification documents stored in the `/specs` folder. Please analyze the current agent definitions and provide recommendations for the following changes:

## 1. Sequential Agent Pipeline

Establish a clear handoff workflow where each agent reads specifications from the previous phase and produces outputs for the next:

1. **software-architect** → Creates initial architectural specifications and design documents
2. **software-engineer** → Implements backend based on architect's specs
3. **verifier** (NEW - needs definition) → Validates backend implementation against architect's specs
4. **qa-engineer** → Writes and executes backend tests, ensures coverage targets are met
5. **frontend-engineer** → Implements frontend based on architect's specs and backend API contracts
6. **verifier** (same agent, different context) → Validates frontend implementation against architect's specs
7. **qa-engineer** → Writes and executes frontend tests (including UI/UX workflows), ensures coverage targets are met

## 2. New Agent Definition Required: Verifier

Create a new agent definition file: `.augment/agents/verifier.md`

**Primary Responsibilities:**
- Compare implementation artifacts (code, structure, patterns) against the original architectural specifications in `/specs` folder
- Identify deviations, missing features, or non-compliance with approved design decisions
- Produce a verification report documenting:
  - ✅ Compliant implementations
  - ⚠️ Deviations with severity assessment (minor/major)
  - ❌ Missing implementations or critical gaps
  - 📝 Recommendations for remediation
- Store verification reports in `/specs/verification/` folder with clear naming (e.g., `backend-verification-report.md`, `frontend-verification-report.md`)

**Context Awareness:**
- When verifying backend (after software-engineer): Focus on service layer, REST endpoints, data models, security implementation, database schema
- When verifying frontend (after frontend-engineer): Focus on component architecture, API integration, routing, state management, accessibility

## 3. API Designer Role Consolidation

**Question for your recommendation:** Should the `api-designer` agent be:
- **Option A:** Merged into `software-architect` (API design as part of architectural planning phase)
- **Option B:** Merged into `software-engineer` (API design as part of implementation phase)
- **Option C:** Kept separate but repositioned in the workflow

**Considerations:**
- API design is a contract between frontend and backend
- OpenAPI specifications should be finalized before implementation begins
- API design decisions have architectural implications (versioning, security, error handling)

Please provide your recommendation with rationale based on best practices for spec-driven development.

## 4. Enhanced Agent Capabilities

### software-engineer and frontend-engineer
- **MUST** use Context 7 MCP tools (`resolve-library-id_Context_7` and `query-docs_Context_7`) to retrieve up-to-date documentation for:
  - Quarkus framework (software-engineer)
  - React, TypeScript, Tailwind CSS (frontend-engineer)
  - Any third-party libraries or frameworks used
- **FALLBACK:** If Context 7 tools fail or are unavailable, use `web-search` tool to find official documentation
- Document which documentation sources were consulted in implementation notes stored in `/specs/implementation-notes/`

### qa-engineer
- **Backend Testing Phase:**
  - Write comprehensive unit and integration tests
  - Execute tests repeatedly until 100% pass rate is achieved
  - Track and report code coverage metrics (target: >80%)
  - Store test reports in `/specs/test-reports/backend-test-report.md`
  
- **Frontend Testing Phase:**
  - Write unit tests for React components
  - **MUST** leverage Chrome DevTools MCP tools (if available) to:
    - Test page navigation flows
    - Validate main user workflows (login, registration, trading operations, etc.)
    - Capture screenshots of UI states
    - Verify responsive design across viewport sizes
  - Execute all tests (unit + integration + UI) repeatedly until 100% pass rate is achieved
  - Track and report frontend code coverage metrics (target: >80%)
  - Store test reports in `/specs/test-reports/frontend-test-report.md`
  
- **Loop Until Success:** The qa-engineer MUST re-run failed tests after fixes and continue the test-fix-retest cycle until all tests pass

## 5. Specification Folder Structure

All agents MUST use the `/specs` folder as the primary communication mechanism:

```
/specs/
├── spec-index.md                           # Master index (maintained by software-architect)
├── phase-01-*.md                           # Architectural specs (software-architect)
├── phase-02-*.md                           # Implementation specs (software-architect)
├── implementation-notes/
│   ├── backend-implementation-notes.md     # software-engineer outputs
│   ├── frontend-implementation-notes.md    # frontend-engineer outputs
│   └── documentation-sources.md            # Context 7 / web search references
├── verification/
│   ├── backend-verification-report.md      # verifier outputs (post-backend)
│   └── frontend-verification-report.md     # verifier outputs (post-frontend)
└── test-reports/
    ├── backend-test-report.md              # qa-engineer outputs (backend phase)
    └── frontend-test-report.md             # qa-engineer outputs (frontend phase)
```

## 6. Agent Definition Updates Required

Please update the following agent definition files:

1. **`.augment/agents/software-architect.md`**
   - Add responsibility for API design (if Option A is chosen)
   - Clarify that specs must define clear acceptance criteria for verifier agent
   
2. **`.augment/agents/software-engineer.md`**
   - Add responsibility for API design (if Option B is chosen)
   - Add mandatory Context 7 documentation retrieval workflow
   - Add requirement to produce implementation notes in `/specs/implementation-notes/`
   
3. **`.augment/agents/frontend-engineer.md`**
   - Add mandatory Context 7 documentation retrieval workflow
   - Add requirement to produce implementation notes in `/specs/implementation-notes/`
   
4. **`.augment/agents/qa-engineer.md`**
   - Add Chrome DevTools MCP integration for frontend testing
   - Add explicit "loop until all tests pass" requirement
   - Add test report generation requirements
   - Clarify dual-phase responsibility (backend tests → frontend tests)
   
5. **`.augment/agents/verifier.md`** (NEW)
   - Create complete agent definition as outlined in section 2 above

6. **`.augment/agents/api-designer.md`**
   - Archive or merge based on your recommendation

## 7. Workflow Validation

After making these changes, please validate that:
- Each agent has a clear input (what specs/artifacts to read) and output (what to produce in `/specs`)
- The handoff between agents is explicit and unambiguous
- The verifier agent has sufficient context to perform meaningful validation
- The qa-engineer has clear success criteria (100% test pass rate, coverage targets)
- All agents reference the `/specs` folder as the single source of truth

Please provide your analysis and recommendations for these changes.
```

### Second enhancement pass

```
Please perform a comprehensive review and enhancement of the agent definitions in `.augment/agents/` to establish a coordinated, specification-driven workflow for application modernization projects. The goal is to create a sequential pipeline where each agent builds upon the previous agent's output.

## Required Agent Workflow

Establish the following sequential workflow:

1. **software-architect** → Creates initial analysis and specifications
2. **software-engineer** (or **quarkus-engineer**) → Implements backend per specifications
3. **verifier** (NEW - needs to be created) → Validates backend implementation against specifications
4. **qa-engineer** → Tests backend implementation (focused on backend test coverage)
5. **frontend-engineer** → Implements frontend per specifications
6. **verifier** (same agent, different context) → Validates frontend implementation against specifications
7. **qa-engineer** → Tests frontend implementation (focused on frontend test coverage)

## New Agent Required: Verifier

Create a new agent definition: `.augment/agents/verifier.md`

**Primary Responsibilities:**
- Verify implementation outcomes from **software-engineer** (backend) or **frontend-engineer** (frontend) against the original specifications created by **software-architect**
- Read specifications from `/specs` folder and compare against actual implementation
- Identify deviations, missing features, or non-compliance with architectural decisions
- Document findings in `/specs/verification-reports/` folder
- Flag critical issues that require immediate attention
- Approve implementation to proceed to QA, or reject and send back to implementation agent

**Key Capabilities:**
- Code analysis and comparison against specifications
- Architectural pattern validation
- API contract verification (backend)
- Component structure validation (frontend)
- Dependency and integration verification
- Generate verification reports in markdown format

## Agent Definition Updates Required

### 1. API Designer Role - Decision Needed

**Question for you:** Should we merge the **api-designer** agent into another role?

**Option A: Merge into software-architect**
- *Rationale:* API design is an architectural decision that should be made during the planning phase
- *Pros:* Ensures API contracts are defined before implementation begins; maintains separation between planning and implementation
- *Cons:* May overload the architect role

**Option B: Merge into software-engineer (quarkus-engineer)**
- *Rationale:* API design is closely tied to implementation and may evolve during development
- *Pros:* Allows for iterative API refinement during implementation; keeps implementation concerns together
- *Cons:* May blur the line between architecture and implementation

**My Recommendation:** Merge **api-designer** into **software-architect**. API design is fundamentally an architectural concern that defines contracts between systems. The architect should define these contracts during the specification phase, and the software-engineer should implement them. This maintains clear separation of concerns and ensures API contracts are reviewed and approved before implementation begins.

**Action Required:** Please confirm your preference, and I will update the agent definitions accordingly.

### 2. software-engineer (quarkus-engineer) Updates

Add the following to the agent definition:

**Documentation Retrieval:**
- **Primary Method:** Use Context 7 MCP tools (`resolve-library-id_Context_7` and `query-docs_Context_7`) to retrieve up-to-date documentation for Quarkus, Jakarta EE, Hibernate, and other Java frameworks/libraries
- **Fallback Method:** If Context 7 is unavailable or doesn't have the required documentation, use the `web-search` tool to find official documentation and guides
- **Version Alignment:** Always retrieve documentation matching the library versions specified in `pom.xml` or `build.gradle`

**Example workflow:**
1. Check `pom.xml` for Quarkus version (e.g., 3.8.0)
2. Use Context 7 to retrieve Quarkus 3.8.0 documentation
3. Reference retrieved documentation when implementing features

### 3. frontend-engineer Updates

Add the following to the agent definition:

**Documentation Retrieval:**
- **Primary Method:** Use Context 7 MCP tools to retrieve up-to-date documentation for React, TypeScript, Vite, Tailwind CSS, and other frontend frameworks/libraries
- **Fallback Method:** If Context 7 is unavailable, use the `web-search` tool to find official documentation
- **Version Alignment:** Always retrieve documentation matching the library versions specified in `package.json`

**Example workflow:**
1. Check `package.json` for React version (e.g., 18.2.0)
2. Use Context 7 to retrieve React 18.2.0 documentation
3. Reference retrieved documentation when implementing components

### 4. qa-engineer Updates

Add the following to the agent definition:

**Frontend Testing with Chrome DevTools:**
- **When testing frontend:** Leverage Chrome DevTools MCP tools to perform browser-based testing of the web frontend
- **Test Coverage:** Navigate through pages, test user workflows, verify UI interactions, check console errors, validate network requests
- **Automated Workflows:** Test critical user journeys (e.g., login → view portfolio → place trade → logout)

**Test Loop Requirement:**
- **Iterative Testing:** Continue running tests until ALL test cases pass
- **Failure Handling:** When tests fail:
  1. Document the failure in `/specs/test-reports/`
  2. Analyze root cause
  3. Report findings to the appropriate agent (software-engineer or frontend-engineer)
  4. Wait for fixes
  5. Re-run tests
  6. Repeat until all tests pass

**Test Coverage Focus:**
- When invoked after **software-engineer**: Focus on backend test coverage (unit tests, integration tests, API tests)
- When invoked after **frontend-engineer**: Focus on frontend test coverage (component tests, integration tests, E2E tests, browser-based tests)

## Specification Storage and Communication

**All agents MUST:**
- Store all outputs, notes, reports, and artifacts in the `/specs` folder
- Use markdown format for all documentation
- Follow a consistent naming convention:
  - Architecture specs: `/specs/phase-XX-<description>.md`
  - Verification reports: `/specs/verification-reports/<agent>-<timestamp>.md`
  - Test reports: `/specs/test-reports/<test-type>-<timestamp>.md`
  - Implementation notes: `/specs/implementation-notes/<feature>-<timestamp>.md`

**Primary Communication Method:**
- Agents communicate by reading and writing markdown files in `/specs`
- Each agent reads the previous agent's output from `/specs` before beginning work
- Each agent writes its output to `/specs` for the next agent to consume

## Application Modernization Context

**software-architect MUST:**
- Recognize that this workflow is designed for **application modernization projects**
- During initial analysis, focus on:
  - Understanding the legacy application architecture
  - Identifying modernization opportunities and challenges
  - Defining the target cloud-native architecture
  - Creating a phased migration plan
  - Documenting legacy-to-modern mapping (e.g., EJB → CDI, JSF → React, etc.)
- Produce specifications that guide the modernization effort, not greenfield development
- Include in specifications:
  - Legacy architecture analysis
  - Target architecture design
  - Migration strategy and phases
  - Risk assessment and mitigation
  - Backward compatibility considerations

**All other agents MUST:**
- Understand they are performing modernization work, not greenfield development
- Preserve business logic and functionality from the legacy application
- Reference legacy code when implementing modern equivalents
- Document mapping between legacy and modern implementations

## Summary of Changes Required

1. **Create:** New `verifier.md` agent definition
2. **Update:** `software-architect.md` to merge in API design responsibilities (if approved)
3. **Update:** `software-engineer.md` (or `quarkus-engineer.md`) to add Context 7 documentation retrieval
4. **Update:** `frontend-engineer.md` to add Context 7 documentation retrieval
5. **Update:** `qa-engineer.md` to add Chrome DevTools testing and test loop requirements
6. **Update:** All agents to emphasize `/specs` folder as primary communication mechanism
7. **Update:** `software-architect.md` to emphasize application modernization context
8. **Remove:** `api-designer.md` (if merging into software-architect)

Please confirm your decision on the API designer role, and I will proceed with implementing all the changes.
```

## Agents README update

```
Now update the .augment/agents/README.md file to reflect this new pipeline of Agents. Add a simple ASCII diagram showing this Agentic pipeline. This README don't need to be much verbose. 
```

## Agent definitions second pass refinement

```
Now based on our conversation so far, please do a second pass on the Agent definitions to check if they capture my intent with this App modernization pipeline - and if you see something is missing or need some refinements, please do it. Try not to break what we already have here. The idea is to catch any missing piece based on my intent so far.
```