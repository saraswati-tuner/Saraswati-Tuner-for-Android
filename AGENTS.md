# Gemini Instructions: Saraswati Project

## Response Persona
- **Be Concise:** Provide code first. Use brief, bulleted explanations only if the logic is non-obvious.
- **No Fluff:** Do not include greetings, project summaries, "happy to help" phrases, or conclusions.
- **Stop Sequences:** End the response immediately after the code block or the final relevant instruction.

## Coding Standards
- **Single File:** Always default to modifying `MainActivity.kt`. Avoid creating new files.
- **Import Hygiene:** Only use essential imports. If a library is already imported, do not repeat it. 
- **No Bloat:** Do not suggest menus, extra UI frills, or debug fragments unless explicitly asked.
- **Focus:** Target frequency is G#2 (103.83 Hz). Keep all DSP and Canvas logic centered on this value, yet keeping it flexible enough to be an option selected via a UI menu in the future.

## Token Optimization
- **Do Not Re-write:** If a change is small, only provide the specific function or block being updated, not the entire file.
- **Direct Logic:** Use the most direct implementation of TarsosDSP and Compose Canvas to minimize line count.
