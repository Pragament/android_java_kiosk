# Firestore Schema: `qb_quiz_submissions_v1`

Stores student quiz attempts submitted from the Android kiosk app.

## Collection

### `qb_quiz_submissions_v1`

Each document is one quiz submission. Documents are created by the app when a verified student taps **Submit** in the quiz activity.

Recommended document id:

- Auto-generated Firestore document id.

## Document Shape

```js
{
  classroomId: '176260',
  sectionId: 'QQAP9O4UyvlaYhqz7jdE',
  admissionNo: '102',
  studentName: 'Parunandi Sai Adithya',

  // Used by the app to fetch one student's submissions.
  studentKey: 'QQAP9O4UyvlaYhqz7jdE_102',

  // Present when a classroom-assigned qb_lists_v1 list was used.
  questionBankListId: 'qb_lists_v1 document id',
  questionBankListName: 'Favorites',

  className: 'IX',
  subject: 'Mathematics',
  chapters: ['Algebra', 'Polynomials'],
  difficulty: 'Easy',

  questionCount: 10,
  answeredCount: 8,
  gradableCount: 7,
  correctCount: 5,

  answers: [
    {
      questionId: 'firestore-question-doc-id',
      type: 'mcq',
      promptHtml: '<p>Question text</p>',

      // Human-readable student answer for review screens.
      displayAnswer: 'Option A',

      // true/false for auto-gradable questions, null for short_answer.
      isCorrect: true,

      // Human-readable correct answer for review screens.
      correctAnswer: 'Option A',

      // MCQ only. Stores selected option indexes from qb_questions_v1.options.
      selectedOptions: [0],

      // FIB only. Stores student-entered answers in blank order.
      fibAnswers: [],

      // True/False only.
      trueFalseAnswer: null,

      // Short-answer only.
      shortAnswer: ''
    }
  ],

  submittedAt: Timestamp,
  submittedAtMillis: 1788264300000
}
```

## Field Notes

- `studentKey` is `${sectionId}_${admissionNo}`. The app queries this field to show past submissions for the verified student.
- `questionBankListId` is set when the submission came from a classroom-assigned `qb_lists_v1` list; otherwise `null`.
- `questionBankListName` snapshots the list name at submission time; otherwise `null`.
- `submittedAt` uses `FieldValue.serverTimestamp()` when Firestore syncs.
- `submittedAtMillis` is written from the device clock so the app can sort submissions even when server timestamp is not yet available.
- `answers` stores enough question and answer content to support reviewing past submissions even if the original question-bank document later changes.
- `isCorrect` is `null` for `short_answer` because the app does not auto-grade descriptive answers.

## Read Access

Recommended:

- A verified student can read submissions where `studentKey` matches their verified section and admission number.
- A teacher/admin can read submissions for classrooms or sections they manage.

## Write Access

Recommended:

- A verified student can create a submission for their own `sectionId`, `admissionNo`, and `studentKey`.
- Updates and deletes should be disabled for students so submitted attempts remain immutable.
- Teacher/admin correction fields can be added later if manual grading is needed.

## Indexes

The app currently fetches by `studentKey` and sorts newest first on the client.

Optional Firestore composite index if server-side sorting is added later:

```js
qb_quiz_submissions_v1:
  studentKey ASC
  submittedAtMillis DESC
```
