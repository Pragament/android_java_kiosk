# Firestore Schema: Student Login And Quiz Flow

This document covers the Firestore collections used by the Android kiosk app when a student verifies classroom access, enters quiz mode, takes a quiz, submits answers, and reviews past submissions.

## Flow Summary

1. Student enters `classCode`, `admissionNo`, and full `phone`.
2. App reads `/classrooms/{classroomId}` directly, or queries `classrooms.where(classCode == enteredCode)`.
3. App allows login only when `classEnabled == true`.
4. App reads `/classSections/{sectionId}/students/{admissionNo}` directly, or queries the section students by `admissionNo`.
5. App shows the last 3 digits of the registered `phone` as a memory hint, then verifies the full entered phone number.
6. If `quizModeEnabled == true`, app opens quiz activity directly and hides homepage apps, websites, and WebView.
7. Quiz activity reads published questions from `qb_questions_v1`.
8. After Start, the quiz runs from the locally loaded question list so the student can continue offline until Submit.
9. App writes the attempt to `qb_quiz_submissions_v1`.
10. App reads `qb_quiz_submissions_v1` by `studentKey` to show past submissions newest first.

## `classrooms`

Stores classroom entry codes, classroom status, section mapping, and mode flags.

### Path

```txt
/classrooms/{classroomId}
```

In the current app, `classroomId` is usually the same as `classCode`, for example `176260`.

### Document Shape

```js
{
  classCode: '176260',
  classEnabled: true,
  className: 'DSS grade 8 aug 31 quiz',

  createdBy: '',
  createdDate: 1788177950135,
  creatorId: 'SPwA523UClVxTpX5m8XPMu5Imiy1',

  quizModeEnabled: true,

  sectionId: 'QQAP9O4UyvlaYhqz7jdE',
  sectionName: 'DSS grade 8'
}
```

### Field Notes

- `classCode`: Code entered by the student.
- `classEnabled`: Student login is blocked unless this is exactly `true`.
- `quizModeEnabled`: When `true`, the app hides homepage apps, websites, and WebView, then opens quiz activity directly.
- `sectionId`: Used to locate student records under `classSections`.

### App Access

- Read during login verification.
- No student writes from the Android kiosk app.

## `classSections`

Stores teacher-managed class sections and section-level student subcollections.

### Path

```txt
/classSections/{sectionId}
```

### Document Shape

```js
{
  createdAt: 1788177721936,
  sectionId: 'QQAP9O4UyvlaYhqz7jdE',
  sectionName: 'DSS grade 8',
  studentCount: 22,
  teacherId: 'SPwA523UClVxTpX5m8XPMu5Imiy1'
}
```

### App Access

- The app does not need the section document for quiz taking, but `sectionId` from `classrooms` points to the `students` subcollection.

## `classSections/{sectionId}/students`

Stores students in a class section. Used for admission number and phone verification.

### Path

```txt
/classSections/{sectionId}/students/{studentDocId}
```

Recommended `studentDocId`:

- Use the admission number, for example `102`.

### Document Shape

```js
{
  admissionNo: '102',
  name: 'Parunandi Sai Adithya',
  phone: '8328303045'
}
```

### Field Notes

- `admissionNo`: Student-entered admission number. The app first checks document id, then falls back to querying this field.
- `phone`: Registered phone number. The app shows the last 3 digits as a hint and verifies the full entered number.
- `name`: Saved into quiz submissions as `studentName`.

### App Access

- Read during login verification.
- No student writes from the Android kiosk app.

## `qb_questions_v1`

Stores all question bank questions used by quiz activity.

### Path

```txt
/qb_questions_v1/{questionId}
```

### Document Shape

```js
{
  type: 'mcq',
  className: 'IX',
  subject: 'Mathematics',
  chapter: 'Algebra',
  topic: 'Polynomials',
  difficulty: 'Easy',
  status: 'published',

  promptHtml: '<p>Question text with rich HTML</p>',

  options: [
    { html: 'Option A rich HTML', correct: true },
    { html: 'Option B rich HTML', correct: false },
    { html: 'Option C rich HTML', correct: false },
    { html: 'Option D rich HTML', correct: false }
  ],

  trueAnswer: true,

  fibBanks: [
    { label: 'Blank 1', answers: ['0', 'zero'] }
  ],

  shortAnswerHtml: '<p>Expected answer</p>',

  translations: {
    hi: {
      question: 'Translated question',
      answer: 'Translated answer',
      options: ['Translated A', 'Translated B', 'Translated C', 'Translated D']
    }
  },

  authorUid: 'firebase-auth-uid',
  authorName: 'Teacher Name',
  createdAt: Timestamp,
  updatedAt: Timestamp,
  archivedAt: Timestamp
}
```

### Question Types

- `mcq`: Uses `options`; app supports one or more correct options.
- `true_false`: Uses `trueAnswer`.
- `fib`: Uses `fibBanks`.
- `short_answer`: Uses `shortAnswerHtml`; app stores the student response but does not auto-grade it.

### App Access

- Quiz activity reads only questions where `status == 'published'`.
- Client-side filtering uses `className`, `subject`, `chapter`, and `difficulty`.
- After the student taps Start, selected questions are shuffled and stored in memory so question navigation can continue offline.

## `qb_quiz_submissions_v1`

Stores student quiz attempts.

### Path

```txt
/qb_quiz_submissions_v1/{submissionId}
```

Recommended `submissionId`:

- Auto-generated Firestore document id.

### Document Shape

```js
{
  classroomId: '176260',
  sectionId: 'QQAP9O4UyvlaYhqz7jdE',
  admissionNo: '102',
  studentName: 'Parunandi Sai Adithya',

  studentKey: 'QQAP9O4UyvlaYhqz7jdE_102',

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

      displayAnswer: 'Option A',
      isCorrect: true,
      correctAnswer: 'Option A',

      selectedOptions: [0],
      fibAnswers: [],
      trueFalseAnswer: null,
      shortAnswer: ''
    }
  ],

  submittedAt: Timestamp,
  submittedAtMillis: 1788264300000
}
```

### Field Notes

- `studentKey`: `${sectionId}_${admissionNo}`. Used to load the verified student's past submissions.
- `answers`: Stores prompt and answer snapshots so reviews still work if a question bank document later changes.
- `isCorrect`: `true` or `false` for auto-graded questions; `null` for `short_answer`.
- `submittedAt`: Server timestamp.
- `submittedAtMillis`: Device timestamp used for client-side newest-first sorting, including pending offline writes.

### App Access

- Create when student taps Submit.
- Read by `studentKey` to show the student's past submissions.
- The app sorts submissions by `submittedAtMillis` newest first on the client.

## Suggested Security Rules

High-level recommendations:

- Allow reading `classrooms` only as needed for code verification.
- Require `classEnabled == true` before allowing student login flows.
- Allow reading `classSections/{sectionId}/students/{studentId}` only for verification or teacher/admin access.
- Allow reading `qb_questions_v1` where `status == 'published'`.
- Allow students to create only their own `qb_quiz_submissions_v1` documents.
- Disable student updates and deletes for quiz submissions.
- Allow teachers/admins to read submissions for their own sections/classes.

## Suggested Indexes

The current app avoids required composite indexes by doing some filtering and sorting client-side.

Optional future indexes:

```js
qb_questions_v1:
  status ASC
  className ASC
  subject ASC
  difficulty ASC

qb_quiz_submissions_v1:
  studentKey ASC
  submittedAtMillis DESC
```
