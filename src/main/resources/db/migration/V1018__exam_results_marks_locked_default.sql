-- NULL marks_locked breaks Hibernate mapping to primitive boolean on exam_results.
UPDATE exam_results SET marks_locked = FALSE WHERE marks_locked IS NULL;
