# ft_turing 🤖

A functional implementation of a Turing Machine simulator in Scala, including a Universal Turing Machine (UTM)!

## 🌐 Try it Online! 

**[Live Demo: Turing Machine Simulator](https://turingmachine.streamlit.app/)** *(Ctrl+click to open in new tab)*

## Overview

This project implements a single-headed, single-tape Turing machine capable of:
- Processing machine descriptions from JSON files
- Simulating standard Turing machines
- Operating as a Universal Turing Machine
- Computing time complexity (bonus feature)

## Features

### Core Functionality
- 🎯 Single-headed, single-tape Turing machine implementation
- 📝 JSON-based machine configuration
- 🔄 Step-by-step visualization of the machine's execution
- ⚡ Adjustable execution speed (up to 10x)
- 🛑 Error detection and handling for invalid inputs or configurations

### Included Machines
1. **Unary Addition**: Computes the sum of two unary numbers
2. **Palindrome Checker**: Determines if input is a palindrome
3. **0ⁿ1ⁿ Language**: Validates if input matches format 0ⁿ1ⁿ
4. **02ⁿ Language**: Validates if input matches format 02ⁿ
5. **Universal Turing Machine**: Simulates the unary addition machine

### Bonus Feature
- Time complexity analysis for executed algorithms
- Visual representation of computational steps
- Complexity classification (O(1), O(n), O(n log n), O(n²), O(n³))

## Web Interface

The web interface (built with Streamlit) provides:
- Interactive machine selection
- Real-time tape visualization
- Speed control
- Dark mode UI
- Responsive design

## Technical Details

### Built With
- Scala 3
- Functional programming principles
- Streamlit (web interface)
- Play JSON for configuration parsing

### Architecture
- Pure functional implementation
- Immutable data structures
- Pattern matching for state transitions
- Type-safe representation of machine configurations

## Academic Context

This project is part of the curriculum at 42 School, focusing on:
- Understanding theoretical computer science
- Implementing complex algorithms
- Working with formal language theory
- Applying functional programming concepts

## Getting Started

### Prerequisites
- Scala 3.x
- sbt

# Run with SBT
sbt run "machines/unary_add.json" "1+1="

## Contact

For any questions or feedback about the project, feel free to reach out!

## Acknowledgments

- Alan Turing, for his groundbreaking work in computer science
- 42 School, for the challenging project subject
- The Scala community for excellent tooling and documentation

---
Happy computing! 🧮✨