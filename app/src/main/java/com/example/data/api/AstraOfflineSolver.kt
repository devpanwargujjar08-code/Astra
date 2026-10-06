package com.example.data.api

object AstraOfflineSolver {

    fun getOfflineSolution(prompt: String): String {
        val query = prompt.lowercase().trim()

        return when {
            query.contains("routine") || query.contains("morning") || query.contains("schedule") -> """
### 🌅 30-Minute Energizing Morning Routine

Here is a practical, step-by-step morning blueprint designed to boost focus and vitality:

1. **Hydration & Sunlight (Minutes 1–5):**
   * Drink 500ml water immediately after waking up.
   * Step near a window or outdoors to anchor your circadian rhythm.

2. **Gentle Mobility & Breathwork (Minutes 6–15):**
   * 5 deep belly breaths (4s in, 4s hold, 6s out).
   * Cat-cow stretches, hamstring stretch, and shoulder rolls.

3. **Mind Priming (Minutes 16–22):**
   * Write down the **Top 1 Most Important Task (MIT)** for today.
   * Note 2 things you are genuinely grateful for.

4. **Nutrient Quick Fuel (Minutes 23–30):**
   * High protein breakfast or clean tea/warm water with lemon.

> *Tip: Keep your phone out of reach for the first 30 minutes to safeguard dopamine and clarity.*
            """.trimIndent()

            query.contains("quantum") || query.contains("physics") -> """
### ⚛️ Quantum Computing Explained Simply

Think of a traditional computer like a regular light switch: it can either be **OFF (0)** or **ON (1)**.

1. **The Qubit Superpower (Superposition):**
   * A quantum bit (qubit) can be **both 0 and 1 at the exact same time**, like a spinning coin before it lands.
   * This allows quantum computers to test millions of possibilities simultaneously.

2. **Entanglement (Cosmic Connection):**
   * Two qubits can become telepathically linked; measuring one immediately determines the state of the other, no matter the distance!

3. **Real-World Impact:**
   * Accelerating discovery of life-saving medicines.
   * Unbreakable next-gen encryption.
   * Optimizing global logistics and clean battery chemistries.
            """.trimIndent()

            query.contains("budget") || query.contains("money") || query.contains("paisa") || query.contains("kharch") -> """
### 💰 The 50/30/20 Smart Budgeting Rule

Apne finances ko effortlessly manage karne ka practical formula:

* **50% — Needs (Zaroori Kharch):**
  Rent, groceries, utilities, travel, aur medical expenses.
* **30% — Wants (Khwahishein & Lifestyle):**
  Dining out, movies, shopping, weekend plans.
* **20% — Savings & Investments (Future Security):**
  Emergency fund, SIP / Mutual funds, index funds, debt payoff.

**Action Steps for this week:**
1. Apne last month ke bank statement download karein.
2. Unwanted auto-renewing subscriptions cancel karein.
3. Salary aate hi sabse pehle 20% savings account ya investment me transfer karein (**Pay Yourself First**).
            """.trimIndent()

            query.contains("python") || query.contains("code") || query.contains("program") -> """
### 🐍 Python Automated File Backup Script

Here is a clean, practical Python script using standard libraries to create automatic timestamped zip backups:

```python
import os
import shutil
from datetime import datetime

def backup_folder(source_dir, backup_destination):
    if not os.path.exists(source_dir):
        print(f"Error: {source_dir} not found.")
        return
    
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    archive_name = f"backup_{timestamp}"
    output_path = os.path.join(backup_destination, archive_name)
    
    # Create zip archive
    zip_result = shutil.make_archive(output_path, 'zip', source_dir)
    print(f"✅ Backup created successfully at: {zip_result}")

# Example usage:
# backup_folder('/path/to/my_projects', '/path/to/backups')
```

**Key Advantages:**
* Uses built-in `shutil` and `os` modules (zero external dependencies).
* Avoids accidental overwrites with precise timestamps.
            """.trimIndent()

            query.contains("brainstorm") || query.contains("hook") || query.contains("idea") -> """
### 💡 5 High-Impact Viral Hook Formulas

Use these proven psychological frameworks for your next content or product pitch:

1. **The Contrarian Truth:**
   *"Most people think [Common Belief], but here is why the top 1% do the exact opposite..."*
2. **The High-Stakes Question:**
   *"What would you do if [Specific Problem] cost you 10 hours every single week?"*
3. **The Transformation Journey:**
   *"How we went from 0 to 10,000 users in 60 days without spending a single rupee on ads."*
4. **The Frictionless Hack:**
   *"If you struggle with [Pain Point], try this 2-minute psychological trick today."*
5. **The Micro-Teaser:**
   *"Don't make this 1 fatal mistake before starting [Goal]..."*

> *Which of these aligns closest with your target audience? Let me know and we will flesh out the complete script!*
            """.trimIndent()

            else -> """
### ✨ Astra Practical Guide: Step-by-Step Approach

Here is a structured, practical approach to help you with **"${prompt.take(45)}"**:

1. **Define the Core Objective:**
   * Break down the main goal into 2–3 actionable, low-friction micro-steps.

2. **Step-by-Step Execution:**
   * **Step A:** Gather all necessary resources or context before diving in.
   * **Step B:** Focus on completing the hardest component first while energy is peak.
   * **Step C:** Review the output against your initial criteria and refine.

3. **Recommended Pro-Tip:**
   * Keep iterations short and measure progress rather than aiming for immediate perfection.

---
*💡 Note: Connect your Gemini API key in **Settings** (or AI Studio Secrets) to unlock Astra's full live real-time AI reasoning across all queries!*
            """.trimIndent()
        }
    }
}
