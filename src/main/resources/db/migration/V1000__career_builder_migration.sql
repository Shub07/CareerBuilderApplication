-- ============================================================
--  Career Builder Module — DB Migration
--  V1000__career_builder_migration.sql
-- ============================================================

-- ── 1. career_assessments ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS career_assessments (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT        NOT NULL,
    title             VARCHAR(200)  NOT NULL,
    description       VARCHAR(500),
    type              VARCHAR(50)   NOT NULL,
    category          VARCHAR(100),
    estimated_minutes INT,
    order_index       INT,
    is_active         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ca_school  ON career_assessments(school_id);
CREATE INDEX IF NOT EXISTS idx_ca_type    ON career_assessments(type);

-- ── 2. career_questions ───────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS career_questions (
    id              BIGSERIAL PRIMARY KEY,
    assessment_id   BIGINT       NOT NULL REFERENCES career_assessments(id) ON DELETE CASCADE,
    question_text   VARCHAR(500) NOT NULL,
    question_order  INT          NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_cq_assessment ON career_questions(assessment_id);
CREATE INDEX IF NOT EXISTS idx_cq_order      ON career_questions(assessment_id, question_order);

-- ── 3. career_question_options ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS career_question_options (
    id           BIGSERIAL PRIMARY KEY,
    question_id  BIGINT       NOT NULL REFERENCES career_questions(id) ON DELETE CASCADE,
    option_text  VARCHAR(300) NOT NULL,
    option_index INT          NOT NULL,
    trait_tag    VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_cqo_question ON career_question_options(question_id);

-- ── 4. career_student_progress ────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS career_student_progress (
    id                      BIGSERIAL PRIMARY KEY,
    student_id              BIGINT      NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    assessment_id           BIGINT      NOT NULL REFERENCES career_assessments(id) ON DELETE CASCADE,
    status                  VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    current_question_index  INT         NOT NULL DEFAULT 0,
    started_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    completed_at            TIMESTAMP,
    last_saved_at           TIMESTAMP,
    CONSTRAINT uk_csp_student_assessment UNIQUE (student_id, assessment_id)
);

CREATE INDEX IF NOT EXISTS idx_csp_student ON career_student_progress(student_id);
CREATE INDEX IF NOT EXISTS idx_csp_status  ON career_student_progress(student_id, status);

-- ── 5. career_student_answers ─────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS career_student_answers (
    id                    BIGSERIAL PRIMARY KEY,
    progress_id           BIGINT    NOT NULL REFERENCES career_student_progress(id) ON DELETE CASCADE,
    question_id           BIGINT    NOT NULL REFERENCES career_questions(id) ON DELETE CASCADE,
    selected_option_index INT       NOT NULL,
    answered_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_csa_progress_question UNIQUE (progress_id, question_id)
);

CREATE INDEX IF NOT EXISTS idx_csa_progress ON career_student_answers(progress_id);

-- ── 6. career_results ─────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS career_results (
    id                       BIGSERIAL PRIMARY KEY,
    student_id               BIGINT  NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    assessment_id            BIGINT  NOT NULL REFERENCES career_assessments(id) ON DELETE CASCADE,
    progress_id              BIGINT  NOT NULL REFERENCES career_student_progress(id) ON DELETE CASCADE,
    strength_areas_json      TEXT,
    interest_indicators_json TEXT,
    score                    INT,
    report_available         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cr_student    ON career_results(student_id);
CREATE INDEX IF NOT EXISTS idx_cr_assessment ON career_results(assessment_id);

-- ============================================================
--  Seed Data — 3 Assessments for school_id = 1
-- ============================================================

-- Assessment 1: Personality Profile (5 questions)
INSERT INTO career_assessments (school_id, title, description, type, category, estimated_minutes, order_index)
VALUES (1, 'Personality Profile', 'Understand your personality traits and work preferences',
        'PERSONALITY_PROFILE', 'Pathway Planners', 30, 1)
ON CONFLICT DO NOTHING;

-- Assessment 2: Aptitude Assessment (5 questions)
INSERT INTO career_assessments (school_id, title, description, type, category, estimated_minutes, order_index)
VALUES (1, 'Aptitude Assessment', 'Evaluate your natural abilities in different skill areas',
        'APTITUDE_ASSESSMENT', 'Pathway Planners', 45, 2)
ON CONFLICT DO NOTHING;

-- Assessment 3: Multiple Intelligence (5 questions)
INSERT INTO career_assessments (school_id, title, description, type, category, estimated_minutes, order_index)
VALUES (1, 'Multiple Intelligence', 'Discover your dominant intelligence types and learning style',
        'MULTIPLE_INTELLIGENCE', 'Pathway Planners', 40, 3)
ON CONFLICT DO NOTHING;

-- ── Questions & Options for Assessment 1 (Personality Profile) ───────────────

-- Q1
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'How do you prefer to solve problems?', 1
FROM career_assessments WHERE title = 'Personality Profile' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('I like to think through all possibilities logically', 0, 'Analytical'),
         ('I prefer hands-on trial and error', 1, 'Technical'),
         ('I discuss with others to get different perspectives', 2, 'Social'),
         ('I research and gather information first', 3, 'Research')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Personality Profile' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 1
ON CONFLICT DO NOTHING;

-- Q2
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'When working in a team, you prefer to:', 2
FROM career_assessments WHERE title = 'Personality Profile' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Take the lead and assign tasks', 0, 'Leadership'),
         ('Collaborate and share ideas equally', 1, 'Social'),
         ('Focus on your part and deliver on time', 2, 'Technical'),
         ('Listen first and then contribute', 3, 'Analytical')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Personality Profile' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 2
ON CONFLICT DO NOTHING;

-- Q3
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'How do you handle deadlines?', 3
FROM career_assessments WHERE title = 'Personality Profile' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Plan ahead and finish early', 0, 'Analytical'),
         ('Work best under pressure', 1, 'Technical'),
         ('Break tasks into smaller steps', 2, 'Research'),
         ('Stay flexible and adjust as needed', 3, 'Creative')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Personality Profile' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 3
ON CONFLICT DO NOTHING;

-- Q4
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'What motivates you most at work?', 4
FROM career_assessments WHERE title = 'Personality Profile' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Solving complex challenges', 0, 'Logical'),
         ('Helping others succeed', 1, 'Social'),
         ('Recognition and growth', 2, 'Leadership'),
         ('Stability and work-life balance', 3, 'Analytical')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Personality Profile' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 4
ON CONFLICT DO NOTHING;

-- Q5
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'When learning something new, you prefer:', 5
FROM career_assessments WHERE title = 'Personality Profile' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Reading and researching first', 0, 'Research'),
         ('Trying it out immediately', 1, 'Technical'),
         ('Watching demos or videos', 2, 'Creative'),
         ('Asking an expert to explain', 3, 'Social')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Personality Profile' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 5
ON CONFLICT DO NOTHING;

-- ── Questions & Options for Assessment 2 (Aptitude Assessment) ───────────────

-- Q1
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'If a train travels 60 km in 45 minutes, what is its speed in km/h?', 1
FROM career_assessments WHERE title = 'Aptitude Assessment' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('80 km/h', 0, 'Analytical'),
         ('90 km/h', 1, 'Logical'),
         ('75 km/h', 2, 'Logical'),
         ('100 km/h', 3, 'Research')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Aptitude Assessment' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 1
ON CONFLICT DO NOTHING;

-- Q2
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'Which word is most opposite in meaning to "Abundant"?', 2
FROM career_assessments WHERE title = 'Aptitude Assessment' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Plentiful', 0, 'Analytical'),
         ('Scarce', 1, 'Logical'),
         ('Ample', 2, 'Social'),
         ('Sufficient', 3, 'Research')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Aptitude Assessment' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 2
ON CONFLICT DO NOTHING;

-- Q3
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'Which shape completes the pattern: Circle, Square, Triangle, Circle, Square, ?', 3
FROM career_assessments WHERE title = 'Aptitude Assessment' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Circle', 0, 'Analytical'),
         ('Triangle', 1, 'Logical'),
         ('Square', 2, 'Research'),
         ('Pentagon', 3, 'Creative')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Aptitude Assessment' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 3
ON CONFLICT DO NOTHING;

-- Q4
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'A map scale shows 1 cm = 5 km. Two cities are 7 cm apart on the map. What is the real distance?', 4
FROM career_assessments WHERE title = 'Aptitude Assessment' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('30 km', 0, 'Analytical'),
         ('35 km', 1, 'Logical'),
         ('40 km', 2, 'Research'),
         ('25 km', 3, 'Technical')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Aptitude Assessment' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 4
ON CONFLICT DO NOTHING;

-- Q5
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'What comes next in the sequence: 2, 6, 12, 20, 30, ?', 5
FROM career_assessments WHERE title = 'Aptitude Assessment' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('40', 0, 'Analytical'),
         ('42', 1, 'Logical'),
         ('44', 2, 'Research'),
         ('36', 3, 'Technical')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Aptitude Assessment' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 5
ON CONFLICT DO NOTHING;

-- ── Questions & Options for Assessment 3 (Multiple Intelligence) ─────────────

-- Q1
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'Which activity do you enjoy most in your free time?', 1
FROM career_assessments WHERE title = 'Multiple Intelligence' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Reading books or solving puzzles', 0, 'Analytical'),
         ('Drawing, painting, or making crafts', 1, 'Artistic'),
         ('Playing sports or doing physical activities', 2, 'Technical'),
         ('Talking with friends or helping people', 3, 'Social')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Multiple Intelligence' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 1
ON CONFLICT DO NOTHING;

-- Q2
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'How do you best remember information?', 2
FROM career_assessments WHERE title = 'Multiple Intelligence' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('By writing it down or reading notes', 0, 'Research'),
         ('By listening to music or rhymes', 1, 'Creative'),
         ('By doing it physically or drawing diagrams', 2, 'Technical'),
         ('By discussing it with someone', 3, 'Social')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Multiple Intelligence' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 2
ON CONFLICT DO NOTHING;

-- Q3
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'Which type of project excites you the most?', 3
FROM career_assessments WHERE title = 'Multiple Intelligence' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Building or fixing something mechanical', 0, 'Technical'),
         ('Writing a story or composing music', 1, 'Creative'),
         ('Organizing and leading a team', 2, 'Leadership'),
         ('Researching and presenting facts', 3, 'Research')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Multiple Intelligence' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 3
ON CONFLICT DO NOTHING;

-- Q4
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'When facing a challenge, your first instinct is to:', 4
FROM career_assessments WHERE title = 'Multiple Intelligence' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('Analyse the situation step by step', 0, 'Analytical'),
         ('Find a creative workaround', 1, 'Artistic'),
         ('Ask others for their opinions', 2, 'Social'),
         ('Look up data or past examples', 3, 'Research')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Multiple Intelligence' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 4
ON CONFLICT DO NOTHING;

-- Q5
INSERT INTO career_questions (assessment_id, question_text, question_order)
SELECT id, 'Your ideal career environment would be:', 5
FROM career_assessments WHERE title = 'Multiple Intelligence' AND school_id = 1 LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO career_question_options (question_id, option_text, option_index, trait_tag)
SELECT q.id, opt.option_text, opt.option_index, opt.trait_tag
FROM career_questions q,
     career_assessments a,
     (VALUES
         ('A lab or tech company', 0, 'Logical'),
         ('A studio or design agency', 1, 'Artistic'),
         ('A school or hospital', 2, 'Social'),
         ('A startup you build yourself', 3, 'Leadership')
     ) AS opt(option_text, option_index, trait_tag)
WHERE a.title = 'Multiple Intelligence' AND a.school_id = 1
  AND q.assessment_id = a.id AND q.question_order = 5
ON CONFLICT DO NOTHING;
