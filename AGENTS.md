# General agent guidelines
## Feature branch
 - Whenever implementing a new feature, fixing an issue/bug create a new feature branch with git.
 - before making any change in the codebase in the main branch pause and ask me if I want to create a new branch to work on.

## Response Formatting
 - Whenever listing or mentioning a file path in the response, MAKE IT a clickable link so I can click it and open it in the Editor. 

## External frameworks and libraries documentation
 - Whenever possible, try to leverage the Context 7 MCP tools to retrieve updated libraries, frameworks, and SDK docs 
 - Try to retrieve documentation thatis  relevant and aligned to the version being used by the project (referenced in the package management)

## Explainability 
- Always list the tools called (if any) during the prompt processing at the end of your response.
- Always list the rules file(s) used (if any) at the end of your prompt response.
- At the end of a task, whenever possible and if it makes sense, provide an estimate (hours, cycles, or sprints, etc) of the human effort to implement the same task from a software maintenance and development perspective.

## Package management
- When creating Python virtual env, prefer `uv` and `uvx` for ephemeral virtual envs.

## Memory usage and recall
- As you go through long tasks in a given session, try to extract and identify patterns and REMEMBER to use your own memory for note-taking. Watch out for important aspects to avoid missing something important mentioned during the conversation. Write it down, reason through it, update it as needed, and make effective use of it.

## Security and Vulnerability Scanning
 - Always run the Snyk Code scanning tool for new first-party code generated.
 - Always run the Snyk SCA scanning tool for new dependencies or dependency updates.
 - If any security issues are found based on newly introduced or modified code or dependencies, attempt to fix the issues using the results context from Snyk.
 - Rescan the code after fixing the issues to ensure that the issues were fixed and that there are no new introduced issues.
 - Repeat this process until no issues are found.

## Task Management
### rules
	  - use Task Lists for any work requiring 3+ distinct steps
	  - Create tasks before starting to make changes, NOT after.
	  - break complex work into specific, hierarchical items organized in Phases
	  - trace progress to give visibility
###	When to use
	  - Multi-step implementation or refactors
	  - Debugging that requires investigating multiple-areas
	  - Feature deployment with several components
	  - Work that spans multiple files, modules, components