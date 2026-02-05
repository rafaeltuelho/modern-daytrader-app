/**
 * Global setup for Vitest - runs BEFORE any test files are loaded
 * This is needed to set up localStorage before authStore module initialization
 */
export function setup() {
  // This runs in Node.js context before tests
  // happy-dom/jsdom will provide localStorage in the test environment
  console.log('Global test setup complete')
}

export function teardown() {
  console.log('Global test teardown complete')
}

