# Spec-Driven Development with Augment Agents

This guide explains how to use the DayTrader Augment agents in a specification-driven development workflow.

## Overview

The spec-driven approach ensures that:
- **Architecture is planned first** before implementation begins
- **User reviews and approves** all architectural decisions
- **Implementation teams** follow approved specifications
- **Deviations are tracked** and require specification updates
- **All work is traceable** back to approved specifications

## Workflow

### Phase 1: Specification Creation

**Agent**: Java Architect

1. **Request Phase Specifications**
   ```
   @java-architect Create Phase 1 specifications for core infrastructure
   ```

2. **Architect Creates Specifications**
   - Breaks down the phase into clear objectives
   - Defines technical approach and design patterns
   - Identifies dependencies and constraints
   - Specifies acceptance criteria
   - Estimates scope and effort

3. **Architect Presents for Review**
   - Writes specifications to `/specs/phase-01-<description>.md`
   - **PAUSES and presents** the plan to you
   - Explicitly invites questions and feedback

### Phase 2: User Review & Approval

**Your Role**: Review and Approve

1. **Review the Specifications**
   - Read the phase specification document
   - Understand the proposed architecture and approach
   - Review acceptance criteria and scope

2. **Ask Questions**
   - Request clarifications on design decisions
   - Ask about trade-offs and alternatives considered
   - Understand the rationale for each decision

3. **Provide Feedback**
   - Suggest modifications or improvements
   - Raise concerns or risks
   - Request explanations of specific approaches

4. **Approve or Request Revisions**
   - Explicitly approve the specifications
   - Or request specific revisions before approval

### Phase 3: Implementation

**Agents**: API Designer, Quarkus Engineer, QA Engineer

1. **API Designer Designs APIs**
   ```
   @api-designer Design REST endpoints per Phase 1 specification
   ```
   - Reads Phase 1 specification
   - Designs APIs aligned with architectural decisions
   - References specification in design documents
   - Flags any deviations from specifications

2. **Quarkus Engineer Implements Services**
   ```
   @quarkus-engineer Implement services per Phase 1 specification
   ```
   - Reads Phase 1 specification
   - Implements services aligned with architectural design
   - References specification in code and commits
   - Flags any deviations from specifications

3. **QA Engineer Creates Tests**
   ```
   @qa-engineer Write tests per Phase 1 specification
   ```
   - Reads Phase 1 specification
   - Creates tests for all acceptance criteria
   - References specification in test code
   - Flags any specification gaps or issues

## Specification Structure

Each phase specification includes:

```markdown
# Phase N: <Title>

## Objectives
- Clear goals for this phase

## Technical Approach
- Design decisions and patterns
- Architecture diagrams
- Code examples

## Dependencies
- Prerequisites
- External dependencies
- Blocked by / Blocks

## Acceptance Criteria
- Measurable success criteria
- Definition of done

## Estimated Scope
- Story points
- Effort estimate
- Timeline

## Risks & Mitigations
- Identified risks
- Mitigation strategies

## Implementation Notes
- Specific guidance for implementation teams
- Key patterns and conventions
- Common pitfalls to avoid
```

## Key Principles

1. **Specifications First**: Always create and approve specifications before implementation
2. **Human-in-the-Loop**: User reviews and approves all architectural decisions
3. **Traceability**: All implementation references approved specifications
4. **Deviation Tracking**: Any deviations from specifications are documented and require approval
5. **Continuous Alignment**: All agents validate alignment with specifications

## Handling Deviations

If implementation reveals the need to deviate from specifications:

1. **Document the Deviation**
   - Clearly explain why the deviation is necessary
   - Document the impact on acceptance criteria

2. **Propose a Specification Update**
   - Suggest changes to the specification
   - Explain the rationale

3. **Wait for Approval**
   - Do NOT proceed with the deviation
   - Wait for user approval before implementing

4. **Update Specifications**
   - Once approved, update the specification document
   - Document the decision and rationale

## Example Workflow

```
User: Create Phase 1 specifications for core infrastructure

Architect: [Creates detailed Phase 1 specification]
           [Presents to user for review]

User: I have questions about the database schema design.
      Can you explain the trade-offs between option A and B?

Architect: [Explains trade-offs and rationale]

User: I'd like to modify the authentication approach.
      Can we use OAuth2 instead of JWT?

Architect: [Updates specification with OAuth2 approach]
           [Presents revised specification]

User: Approved! Please proceed with implementation.

API Designer: [Reads Phase 1 specification]
              [Designs REST endpoints per spec]
              [References specification in design]

Quarkus Engineer: [Reads Phase 1 specification]
                  [Implements services per spec]
                  [References specification in code]

QA Engineer: [Reads Phase 1 specification]
             [Creates tests for acceptance criteria]
             [References specification in tests]
```

## Best Practices

1. **Be Specific**: Provide clear, detailed specifications
2. **Include Examples**: Use code examples to illustrate patterns
3. **Document Trade-offs**: Explain why each decision was made
4. **Plan Phases**: Break work into discrete, manageable phases
5. **Review Thoroughly**: Take time to review and understand specifications
6. **Ask Questions**: Don't hesitate to ask for clarifications
7. **Reference Specs**: Always reference specifications in implementation
8. **Flag Issues**: Report any specification gaps or issues discovered during implementation

