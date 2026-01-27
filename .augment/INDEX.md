# Augment CLI Agents - Complete Index

## 📋 Overview

This directory contains Augment CLI subagent definitions for the DayTrader Java modernization project, configured to follow a **specification-driven development approach**.

## 🚀 Quick Navigation

### For First-Time Users
1. **Start here**: [`QUICK_START.md`](QUICK_START.md) - 5-minute overview
2. **Then read**: [`agents/README.md`](agents/README.md) - Agent overview and usage
3. **Visual guide**: [`VISUAL_GUIDE.md`](VISUAL_GUIDE.md) - Workflow diagrams

### For Architects
1. **Workflow guide**: [`SPEC_DRIVEN_DEVELOPMENT.md`](SPEC_DRIVEN_DEVELOPMENT.md) - Detailed workflow
2. **Spec template**: [`PHASE_SPECIFICATION_TEMPLATE.md`](PHASE_SPECIFICATION_TEMPLATE.md) - Template for creating specs
3. **Agent definition**: [`agents/java-architect.md`](agents/java-architect.md) - Architect agent details

### For Implementation Teams
1. **Agent overview**: [`agents/README.md`](agents/README.md) - All agents overview
2. **API Designer**: [`agents/api-designer.md`](agents/api-designer.md) - API design agent
3. **Quarkus Engineer**: [`agents/quarkus-engineer.md`](agents/quarkus-engineer.md) - Implementation agent
4. **QA Engineer**: [`agents/qa-engineer.md`](agents/qa-engineer.md) - Testing agent

### For Technical Leads
3. **Visual guide**: [`VISUAL_GUIDE.md`](VISUAL_GUIDE.md) - Workflow diagrams

## 📁 File Structure

```
.augment/
├── agents/                          # Agent definitions
│   ├── README.md                    # Agent overview (UPDATED)
│   ├── java-architect.md            # Architect agent (UPDATED)
│   ├── api-designer.md              # API Designer agent (UPDATED)
│   ├── quarkus-engineer.md          # Quarkus Engineer agent (UPDATED)
│   └── qa-engineer.md               # QA Engineer agent (UPDATED)
│
├── Documentation Files
│   ├── INDEX.md                     # This file
│   ├── QUICK_START.md               # 5-minute overview
│   ├── VISUAL_GUIDE.md              # Workflow diagrams
│   ├── SPEC_DRIVEN_DEVELOPMENT.md   # Detailed workflow guide
│   ├── PHASE_SPECIFICATION_TEMPLATE.md # Spec template
│
└── /specs/                          # Specification folder (to be created)
    ├── spec-index.md                # Master index
    ├── phase-01-*.md                # Phase 1 specification
    ├── phase-02-*.md                # Phase 2 specification
    ├── phase-03-*.md                # Phase 3 specification
    └── phase-04-*.md                # Phase 4 specification
```

## 📚 Documentation Guide

| Document | Purpose | Read Time | Audience |
|----------|---------|-----------|----------|
| **QUICK_START.md** | 5-minute overview of spec-driven approach | 5 min | Everyone |
| **agents/README.md** | Agent overview and usage guide | 10 min | Everyone |
| **VISUAL_GUIDE.md** | Workflow diagrams and visual explanations | 10 min | Everyone |
| **SPEC_DRIVEN_DEVELOPMENT.md** | Detailed workflow and best practices | 20 min | Architects, Leads |
| **PHASE_SPECIFICATION_TEMPLATE.md** | Template for creating phase specifications | 10 min | Architects |
| **agents/java-architect.md** | Java Architect agent definition | 10 min | Architects |
| **agents/api-designer.md** | API Designer agent definition | 10 min | API Designers |
| **agents/quarkus-engineer.md** | Quarkus Engineer agent definition | 10 min | Developers |
| **agents/qa-engineer.md** | QA Engineer agent definition | 10 min | QA Engineers |

## 🎯 Key Features

✅ **Specification-Driven Development** - All work guided by approved specifications
✅ **Human-in-the-Loop Review** - User reviews and approves all architectural decisions
✅ **Phase-Based Planning** - Work organized into discrete, manageable phases
✅ **Traceability** - All implementation references approved specifications
✅ **Deviation Tracking** - Deviations documented and managed
✅ **Comprehensive Documentation** - Multiple guides and templates
✅ **Clear Workflow** - Well-defined process from specification to implementation

## 🔄 Workflow Summary

```
1. Java Architect creates Phase specifications
2. Architect PAUSES and presents to user
3. User reviews and approves specifications
4. API Designer designs APIs per specification
5. Quarkus Engineer implements per specification
6. QA Engineer creates tests per specification
7. All agents reference and align with specifications
```

## 🚀 Getting Started

### Step 1: Understand the Approach (5 minutes)
```bash
Read: QUICK_START.md
```

### Step 2: Learn About Agents (10 minutes)
```bash
Read: agents/README.md
```

### Step 3: Create Phase 1 Specifications
```bash
@java-architect Create Phase 1 specifications for core infrastructure
```

### Step 4: Review & Approve
- Read the specification
- Ask questions
- Approve or request revisions

### Step 5: Implement Per Specifications
```bash
@api-designer Design REST endpoints per Phase 1 specification
@quarkus-engineer Implement services per Phase 1 specification
@qa-engineer Write tests per Phase 1 specification
```

## 📞 Need Help?

- **Quick overview?** → Read `QUICK_START.md`
- **Agent details?** → Read `agents/README.md`
- **Workflow guide?** → Read `SPEC_DRIVEN_DEVELOPMENT.md`
- **Visual explanation?** → Read `VISUAL_GUIDE.md`
- **Creating specs?** → Use `PHASE_SPECIFICATION_TEMPLATE.md`

## ✅ Status

All agents have been refactored to follow the specification-driven development approach. All documentation has been created and is ready to use.

---

**Last Updated**: 2026-01-27
**Version**: 1.0
**Status**: Ready for Use

