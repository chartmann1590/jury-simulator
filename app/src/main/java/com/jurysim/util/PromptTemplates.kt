package com.jurysim.util

object PromptTemplates {

    fun generateCase(): String = """
        You are a legal case generator for a courtroom simulation. Generate a realistic criminal court case with the following details:

        1. Case title (e.g., "The People vs. John Smith")
        2. Defendant's full name
        3. Criminal charges (be specific, realistic)
        4. Brief case description (2-3 sentences explaining what allegedly happened)
        5. Key evidence that will be presented

        Format your response EXACTLY as follows:
        CASE_TITLE: [case title]
        DEFENDANT: [defendant name]
        CHARGES: [criminal charges]
        DESCRIPTION: [case description]
        EVIDENCE: [list of key evidence items]

        Make it realistic and engaging. Choose from various crime types (theft, assault, fraud, etc.).
    """.trimIndent()

    fun voirDireIntro(
        caseTitle: String,
        charges: String,
        jurorName: String,
        jurorOccupation: String,
        jurorAge: Int,
        hasLegalExperience: Boolean
    ): String = """
        You are the presiding judge in the case: $caseTitle.
        The defendant is charged with: $charges

        This is the jury selection phase (voir dire). You will ask the potential juror questions to determine if they are suitable for this case.

        Potential Juror Information:
        - Name: $jurorName
        - Age: $jurorAge
        - Occupation: $jurorOccupation
        - Legal Experience: ${if (hasLegalExperience) "Yes" else "No"}

        Ask 3-5 questions, one at a time, to assess:
        - Any biases or prejudices
        - Ability to be fair and impartial
        - Understanding of "innocent until proven guilty"
        - Any conflicts of interest
        - Relevant personal experiences

        You may reference their occupation or background in your questions to make it more personal and realistic.

        Start by introducing yourself as Judge [make up a name] and ask your first question in a professional, courtroom manner.
        Keep it brief and conversational.
    """.trimIndent()

    fun voirDireQuestion(previousConversation: String): String = """
        Continue the jury selection (voir dire) process.

        Previous conversation:
        $previousConversation

        Based on their answer, ask your next question OR make your final decision.

        If you've asked enough questions (3-5), provide your decision:
        - If they seem suitable: Say "SELECTED: You have been selected to serve on the jury."
        - If not suitable: Say "DISMISSED: Thank you for your time, but you are dismissed from jury duty."

        Otherwise, ask your next question naturally.
    """.trimIndent()

    fun openingStatement(side: String, caseDescription: String, charges: String, defendantName: String): String = """
        You are the $side attorney in a criminal trial.

        Defendant: $defendantName
        Charges: $charges
        Case background: $caseDescription

        Deliver your opening statement to the jury. This should be:
        - Compelling and persuasive
        - 3-4 paragraphs
        - Outline what you will prove
        - Set the tone for your case

        ${if (side == "Prosecution") "Focus on the evidence that proves guilt beyond a reasonable doubt."
          else "Focus on raising reasonable doubt and defending your client's innocence."}

        Begin with: "Ladies and gentlemen of the jury..."
    """.trimIndent()

    fun generateWitness(
        witnessNumber: Int,
        totalWitnesses: Int,
        side: String,
        caseDescription: String,
        defendantName: String
    ): String = """
        Generate a witness for the $side side (witness $witnessNumber of $totalWitnesses).

        Case: $caseDescription
        Defendant: $defendantName

        Provide:
        1. Witness name and role (e.g., "Detective Sarah Johnson, Lead Investigator")
        2. Brief testimony (2-3 paragraphs) that this witness would provide

        Format:
        WITNESS: [name and role]
        TESTIMONY: [their testimony]

        Make the witness and testimony realistic and relevant to the case.
        ${if (side == "Prosecution") "This witness should support the prosecution's case."
          else "This witness should support the defense."}
    """.trimIndent()

    fun crossExamination(witnessName: String, testimony: String, side: String): String = """
        You are the $side attorney.

        The opposing side just presented witness: $witnessName
        Their testimony: $testimony

        Ask 2-3 cross-examination questions to challenge their testimony or credibility.
        Present them as a numbered list, conversational and strategic.
    """.trimIndent()

    fun presentEvidence(caseDescription: String, defendantName: String): String = """
        You are the prosecutor presenting physical evidence in this case:

        Case: $caseDescription
        Defendant: $defendantName

        Present 3-4 pieces of physical evidence (e.g., DNA, fingerprints, surveillance footage, documents, weapons, etc.).

        For each piece of evidence:
        1. Describe it
        2. Explain how it was obtained
        3. Explain its significance to the case

        Format each as:
        EVIDENCE [number]: [name]
        DESCRIPTION: [details]
        SIGNIFICANCE: [why it matters]

        Make it realistic and impactful.
    """.trimIndent()

    fun closingArgument(side: String, caseDescription: String, defendantName: String, charges: String): String = """
        You are the $side attorney delivering your closing argument.

        Defendant: $defendantName
        Charges: $charges
        Case: $caseDescription

        This is your final chance to persuade the jury. Deliver a powerful 3-4 paragraph closing argument that:
        - Summarizes the key evidence
        - Addresses the jury's duty
        - Makes your final appeal

        ${if (side == "Prosecution")
          "Emphasize that the evidence proves guilt beyond a reasonable doubt. Ask for a guilty verdict."
          else
          "Emphasize reasonable doubt. Ask for a not guilty verdict."}

        Begin with: "Ladies and gentlemen of the jury..."
    """.trimIndent()

    fun deliberationStart(
        caseDescription: String,
        defendantName: String,
        charges: String,
        jurors: List<com.jurysim.data.model.AIJuror>
    ): String = """
        You are simulating a jury deliberation room with 11 other jurors (the user is the 12th).

        Case: $caseDescription
        Defendant: $defendantName
        Charges: $charges

        Here are the other jurors in the room:
        ${jurors.joinToString("\n") { "ID ${it.id}: ${it.name} (${it.occupation}), Personality: ${it.personality}, Hidden Bias: ${it.hiddenBias}, Initial Leaning: ${it.initialLeaning}" }}

        Generate the initial reactions from 3 different jurors with distinct perspectives.
        STRICTLY usage the names and personas provided above. Do not invent new jurors.

        Format each as:
        [Juror Name]: [their opinion, 1-2 sentences]

        Make them realistic, thoughtful, and reference specific evidence or testimony.
        Keep it conversational.
    """.trimIndent()

    fun deliberationResponse(
        conversation: String,
        userInput: String,
        jurors: List<com.jurysim.data.model.AIJuror>
    ): String = """
        Continue the jury deliberation simulation.

        Previous discussion:
        $conversation

        User's latest input: $userInput

        Here are the jurors:
        ${jurors.joinToString("\n") { "ID ${it.id}: ${it.name} (${it.occupation}), Personality: ${it.personality}, Hidden Bias: ${it.hiddenBias}, Leaning: ${it.initialLeaning}" }}

        Generate responses from 2-3 other jurors reacting to what the user said.
        STRICTLY usage the names and personas provided above.
        Keep the discussion moving toward a consensus.

        After 4-5 exchanges, if a consensus is forming, have one juror suggest: "I think we're ready to vote."

        Format:
        [Juror Name]: [their response]
    """.trimIndent()

    fun finalVerdict(conversation: String): String = """
        Based on the jury deliberation:

        $conversation

        Determine the final verdict. Respond with ONLY:
        VERDICT: GUILTY
        or
        VERDICT: NOT GUILTY

        Then provide a 2-3 sentence explanation of the key factors that led to this decision.

        Format:
        VERDICT: [GUILTY or NOT GUILTY]
        REASONING: [explanation]
    """.trimIndent()

    fun generateAIJurors(): String = """
        Generate 11 diverse jurors for a criminal trial jury. Each juror should have a distinct personality and background.

        For each juror, provide:
        1. Name (realistic, diverse names)
        2. Age (18-80)
        3. Occupation
        4. Personality trait (e.g., analytical, emotional, skeptical, empathetic, pragmatic, etc.)
        5. A "Hidden Bias" or secret backstory that might influence their decision (e.g., "Brother was falsely accused", "Trusts authority implicitly", "Victim of similar crime").
        6. Initial leaning (LEANING_GUILTY, LEANING_NOT_GUILTY, or UNDECIDED)

        Make sure to have a mix of:
        - 3-4 jurors leaning guilty
        - 3-4 jurors leaning not guilty
        - 3-4 undecided jurors

        Format EXACTLY as follows for each juror:
        JUROR_1:
        NAME: [name]
        AGE: [age]
        OCCUPATION: [occupation]
        PERSONALITY: [personality trait]
        HIDDEN_BIAS: [hidden bias description]
        LEANING: [LEANING_GUILTY/LEANING_NOT_GUILTY/UNDECIDED]

        Continue for JUROR_2 through JUROR_11.
    """.trimIndent()

    fun individualJurorChat(
        jurorName: String,
        jurorOccupation: String,
        jurorPersonality: String,
        jurorHiddenBias: String,
        jurorLeaning: String,
        caseDescription: String,
        defendantName: String,
        charges: String,
        previousConversation: String,
        userInput: String
    ): String = """
        You are $jurorName, a $jurorOccupation serving on a jury.

        Your personality: $jurorPersonality
        Your hidden bias/secret: $jurorHiddenBias
        Your initial thoughts on the case: $jurorLeaning

        Case: $caseDescription
        Defendant: $defendantName
        Charges: $charges

        Previous conversation with fellow juror (the user):
        $previousConversation

        The user just said: "$userInput"

        Respond as $jurorName would, based on your personality and hidden bias. Keep your response natural and conversational (2-4 sentences).
        Stay in character. Your hidden bias should subtly influence your thinking, but don't explicitly state "I have a hidden bias".
        If the user is trying to persuade you, you may gradually shift your opinion but stay true to your personality.

        Do NOT include your name at the start of the response - just respond directly.
    """.trimIndent()

    fun jurorVote(
        jurorName: String,
        jurorPersonality: String,
        jurorHiddenBias: String,
        jurorLeaning: String,
        caseDescription: String,
        defendantName: String,
        charges: String,
        deliberationSummary: String,
        votingRound: Int
    ): String = """
        You are $jurorName, a juror in this trial.

        Your personality: $jurorPersonality
        Your hidden bias: $jurorHiddenBias
        Your initial leaning: $jurorLeaning

        Case: $caseDescription
        Defendant: $defendantName
        Charges: $charges

        Summary of deliberation so far:
        $deliberationSummary

        This is voting round $votingRound. Based on the deliberation, your personality, and your hidden bias, cast your vote.

        Consider:
        - The evidence presented
        - Arguments made by other jurors
        - Your personal perspective and reasoning (influenced by your bias)
        - Whether reasonable doubt exists

        You may change your vote from previous rounds if you've been convinced.

        Respond with ONLY:
        VOTE: GUILTY
        or
        VOTE: NOT_GUILTY

        Then a brief 1-sentence explanation.

        Format:
        VOTE: [GUILTY or NOT_GUILTY]
        REASON: [brief explanation]
    """.trimIndent()

    fun mistrialAnnouncement(
        caseTitle: String,
        votingRounds: Int
    ): String = """
        The jury in the case "$caseTitle" was unable to reach a unanimous verdict after $votingRounds rounds of voting.

        Generate a brief judge's announcement declaring a mistrial. Include:
        1. Acknowledgment of the jury's efforts
        2. Explanation that a unanimous verdict could not be reached
        3. What happens next (case may be retried)

        Keep it formal and realistic (2-3 paragraphs).
    """.trimIndent()

    fun generateAINotes(
        caseDescription: String,
        defendantName: String,
        charges: String,
        witnessTestimony: String,
        evidencePresented: String
    ): String = """
        As a helpful AI assistant, generate comprehensive trial notes for a juror based on the case information.

        Case: $caseDescription
        Defendant: $defendantName
        Charges: $charges

        Witness Testimony Summary:
        $witnessTestimony

        Evidence Presented:
        $evidencePresented

        Generate organized notes that include:
        1. Key facts of the case
        2. Important witness statements and their credibility
        3. Physical evidence and its significance
        4. Points favoring the prosecution
        5. Points favoring the defense
        6. Questions to consider during deliberation

        Format the notes clearly with bullet points and headers. Keep it concise but comprehensive.
    """.trimIndent()

    fun parseEvidence(evidenceResponse: String): String = """
        Parse the following evidence presentation and extract individual evidence items:

        $evidenceResponse

        For each piece of evidence, format as:
        EVIDENCE_ITEM:
        NAME: [evidence name]
        DESCRIPTION: [description]
        SIGNIFICANCE: [why it matters]
        PRESENTED_BY: Prosecution

        Extract all evidence items mentioned.
    """.trimIndent()

    fun judgeRuling(
        caseDescription: String,
        defendantName: String,
        charges: String,
        currentWitness: String,
        question: String
    ): String = """
        You are the Judge in this trial.
        
        Case: $caseDescription
        Defendant: $defendantName
        Charges: $charges
        Current Witness on stand: $currentWitness
        
        A juror has submitted a question for the witness: "$question"
        
        Decide if this question is relevant and permissible.
        
        If PERMISSIBLE:
        Respond with "RULING: ALLOWED"
        
        If NOT PERMISSIBLE (e.g., irrelevant, asking for hearsay, speculative, or argumentative):
        Respond with "RULING: DENIED" followed by a brief explanation of why (1 sentence).
        
        Format:
        RULING: [ALLOWED/DENIED]
        REASON: [If denied, explain why]
    """.trimIndent()

    fun witnessCrossExam(
        witnessName: String,
        caseDescription: String,
        defendantName: String,
        question: String
    ): String = """
        You are the witness $witnessName testifying in court.
        
        Case: $caseDescription
        Defendant: $defendantName
        
        The judge has allowed a juror's question: "$question"
        
        Answer the question in character. Keep it consistent with your previous testimony (if any) or generate a plausible answer that fits the case facts.
        
        Respond directly as the witness (1-3 sentences).
    """.trimIndent()
}
