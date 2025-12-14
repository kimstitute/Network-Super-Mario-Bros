# Network Super Mario Bros - Development Plan

## Project Overview

### Original Game
- **Source**: Classic Super Mario Bros game implemented in Java
- **Original Repository**: https://github.com/ahmetcandiroglu/Super-Mario-Bros
- **Base Implementation**: Single-player platformer game with complete game mechanics

### Course Requirements
- **Course**: Network Programming (네트워크프로그래밍)
- **Deadline**: December 14, 2024 (midnight)
- **Team**: Individual project
- **Objective**: Extend existing single-player game to network multiplayer using Java socket programming

### Deliverables
1. Final Report (PPT) with contribution breakdown
2. Demo Video (5-10 minutes) - feature explanation + code review
3. Final Source Code (complete Eclipse project)
4. Original Source Code + Original execution video (open-source usage)

### Evaluation Criteria (각 20%)
- Feature Completeness
- Difficulty & Creativity
- Code Understanding (code review)
- Code Quality (readability, maintainability, encapsulation)
- Collaboration & Submission Integrity

---

## Technical Stack

### Core Technologies
- **Language**: Java
- **GUI Framework**: Java Swing
- **Networking**: Java Socket Programming (TCP/UDP)
- **Build Tool**: Manual compilation (Eclipse project)
- **Version Control**: Git + GitHub

### Project Structure
```
src/
├── manager/           # Game logic management
│   ├── GameEngine.java
│   ├── MapManager.java
│   ├── SoundManager.java
│   ├── InputManager.java
│   └── Camera.java
├── model/            # Game objects
│   ├── hero/        # Mario, Fireball
│   ├── enemy/       # Goomba, KoopaTroopa
│   ├── brick/       # Blocks and pipes
│   └── prize/       # Items and power-ups
├── view/            # UI and rendering
│   ├── UIManager.java
│   ├── ImageLoader.java
│   └── Animation.java
└── network/         # Network layer (TO BE IMPLEMENTED)
    ├── server/
    ├── client/
    └── protocol/
```

---

## Architecture Design

### Network Architecture (TO BE DESIGNED)

**Client-Server Model**
- Server: Game state authority, physics simulation, collision detection
- Client: Rendering, input handling, state synchronization

**Communication Protocol**
- Transport: TCP for reliable state sync, UDP for real-time input (TBD)
- Message Format: Custom protocol for game events

**Synchronization Strategy**
- Server-authoritative model
- Client-side prediction (optional)
- Lag compensation (optional)

### Design Decisions (TO BE MADE)
- [ ] TCP vs UDP vs Hybrid approach
- [ ] Game state synchronization frequency
- [ ] Handling network latency
- [ ] Player disconnection handling
- [ ] Game lobby system

---

## Implementation Plan

### Phase 1: Analysis & Design (Current)
**Goal**: Understand existing codebase and design network architecture

**Tasks**:
- [x] Set up project repository
- [x] Initialize Git and push to GitHub
- [x] Set up development documentation
- [ ] Analyze existing game architecture
- [ ] Design network communication protocol
- [ ] Design client-server architecture
- [ ] Create UML diagrams (sequence, class)

### Phase 2: Core Network Infrastructure
**Goal**: Implement basic client-server communication

**Tasks**:
- [ ] Implement server socket
- [ ] Implement client socket
- [ ] Define custom protocol (message format)
- [ ] Test basic connection/disconnection
- [ ] Implement game lobby system

### Phase 3: Game State Synchronization
**Goal**: Synchronize game state between server and clients

**Tasks**:
- [ ] Refactor GameEngine for network support
- [ ] Implement server-side game loop
- [ ] Implement state serialization/deserialization
- [ ] Synchronize player positions
- [ ] Synchronize enemy positions
- [ ] Synchronize item states

### Phase 4: Multiplayer Features
**Goal**: Add multiplayer-specific game mechanics

**Tasks**:
- [ ] Multiple player rendering
- [ ] Player interaction (cooperation/competition)
- [ ] Scoring system for multiplayer
- [ ] Win/lose conditions
- [ ] Player name display

### Phase 5: Testing & Polish
**Goal**: Bug fixes, optimization, and documentation

**Tasks**:
- [ ] Network latency testing
- [ ] Edge case handling (disconnection, timeout)
- [ ] Performance optimization
- [ ] Code cleanup and refactoring
- [ ] Code documentation

### Phase 6: Presentation Materials
**Goal**: Prepare deliverables

**Tasks**:
- [ ] Record demo video
- [ ] Prepare final report PPT
- [ ] Document source file list and execution order
- [ ] Prepare original source comparison
- [ ] Write contribution breakdown

---

## Development Guidelines

### Coding Conventions
```java
// Class naming: PascalCase
public class NetworkManager { }

// Method naming: camelCase
public void handlePlayerInput() { }

// Constants: UPPER_SNAKE_CASE
private static final int MAX_PLAYERS = 4;

// Package naming: lowercase
package network.server;
```

### Git Workflow
```
1. Work on feature branches
2. Commit frequently with descriptive messages
3. Format: "feat(scope): description"
   - feat: new feature
   - fix: bug fix
   - refactor: code refactoring
   - docs: documentation
   - test: testing
4. Push to GitHub regularly
```

### Commit Message Examples
```
feat(network): implement basic server socket
fix(sync): resolve player position desync issue
refactor(protocol): simplify message format
docs(architecture): add network design diagrams
```

---

## Key Features to Implement

### Must-Have Features
1. **Network Connection**
   - Server-client connection establishment
   - Multiple client support (2-4 players)

2. **Game State Sync**
   - Player position synchronization
   - Game object state synchronization

3. **Multiplayer Gameplay**
   - Multiple players visible on screen
   - Basic interaction between players

4. **Game Lobby**
   - Player can host/join games
   - Display connected players

### Nice-to-Have Features
1. **Advanced Sync**
   - Client-side prediction
   - Lag compensation

2. **Enhanced Gameplay**
   - Cooperative mechanics (shared lives, combined score)
   - Competitive mechanics (race to finish, score competition)

3. **Polish**
   - Player customization (colors, names)
   - Chat system
   - Reconnection handling

---

## Current Progress

### Completed
- [x] Repository setup
- [x] Git initialization and GitHub push
- [x] Development documentation structure

### In Progress
- [ ] Codebase analysis
- [ ] Network architecture design

### Blocked
- None

---

## Technical Notes

### Network Protocol Design (Draft)

**Message Types**:
```java
// Client → Server
CONNECT(playerId, playerName)
INPUT(playerId, keyCode, pressed)
DISCONNECT(playerId)

// Server → Client
GAME_STATE(players[], enemies[], items[])
PLAYER_JOINED(playerId, playerName)
PLAYER_LEFT(playerId)
GAME_STARTED()
GAME_ENDED(winner)
```

**Message Format** (TBD):
- Option 1: JSON strings
- Option 2: Binary protocol (custom serialization)
- Option 3: Java Serialization

### Performance Considerations
- Target tick rate: 60 FPS (client rendering)
- Network update rate: 20-30 Hz (server → client)
- Input send rate: On input change (client → server)

### Known Issues
- None yet

---

## Resources

### Documentation
- Original Game Repository: https://github.com/ahmetcandiroglu/Super-Mario-Bros
- Course Materials: Network Programming lecture notes
- Java Socket Programming: Official Oracle documentation

### References
- Network game development patterns
- Client-server architecture best practices
- Java networking tutorials

---

## Questions & Decisions Log

### Open Questions
1. Should we use TCP, UDP, or hybrid approach?
2. How many players should we support? (2-4?)
3. What multiplayer mode? (cooperative vs competitive vs both?)
4. How to handle different network conditions?

### Decisions Made
- Project repository: https://github.com/kimstitute/Network-Super-Mario-Bros
- Development approach: Extend existing codebase
- Documentation: Markdown-based in `.claude/` directory

---

## Daily Progress Log

### 2024-12-14
- **Completed**:
  - Created new GitHub repository
  - Initialized Git and pushed initial commit
  - Set up `.claude/` directory structure
  - Created development plan document (CLAUDE.md)
  - Configured project settings

- **Next Steps**:
  - Analyze existing codebase architecture
  - Design network communication protocol
  - Start implementation planning

---

**Last Updated**: 2024-12-14
**Status**: Planning Phase
**Next Milestone**: Complete architecture design
