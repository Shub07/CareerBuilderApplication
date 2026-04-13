package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(
		name = "schools",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_school_code", columnNames = "school_code"),
				@UniqueConstraint(name = "uk_affiliation_number", columnNames = "affiliation_number")
		},
		indexes = {
				@Index(name = "idx_school_district", columnList = "district"),
				@Index(name = "idx_school_state", columnList = "state_ut")
		}
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // Required by JPA
@AllArgsConstructor(access = AccessLevel.PRIVATE)   // Prevents misuse
@Builder
public class School {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, updatable = false)
	private Long id;

	@NotBlank(message = "School name is required")
	@Size(min = 2, max = 200)
	@Column(name = "school_name", nullable = false, length = 200)
	private String schoolName;

	/**
	 * Unique school code provided by education authority.
	 */
	@NotBlank(message = "School code is required")
	@Size(min = 2, max = 50)
	@Column(name = "school_code", nullable = false, length = 50)
	private String schoolCode;

	/**
	 * Government / Private / Aided etc.
	 */
	@NotBlank(message = "School type is required")
	@Size(max = 50)
	@Column(name = "school_type", nullable = false, length = 50)
	private String schoolType;

	/**
	 * CBSE / ICSE / State Board etc.
	 */
	@NotBlank(message = "Board affiliation is required")
	@Size(max = 50)
	@Column(name = "board_affiliation", nullable = false, length = 50)
	private String boardAffiliation;

	/**
	 * Unique board affiliation number.
	 */
	@NotBlank(message = "Affiliation number is required")
	@Size(max = 50)
	@Column(name = "affiliation_number", nullable = false, length = 50)
	private String affiliationNumber;

	@NotBlank(message = "Year of establishment is required")
	@Size(min = 4, max = 4)
	@Column(name = "year_of_establishment", nullable = false, length = 4)
	private String yearOfEstablishment;

	@NotBlank(message = "Medium of instruction is required")
	@Size(max = 50)
	@Column(name = "medium_of_instruction", nullable = false, length = 50)
	private String mediumOfInstruction;

	@NotBlank(message = "School category is required")
	@Size(max = 50)
	@Column(name = "school_category", nullable = false, length = 50)
	private String schoolCategory;

	@NotBlank(message = "Village/Town/City is required")
	@Size(max = 100)
	@Column(name = "village_town_city", nullable = false, length = 100)
	private String villageTownCity;

	@NotBlank(message = "District is required")
	@Size(max = 100)
	@Column(name = "district", nullable = false, length = 100)
	private String district;

	@NotBlank(message = "State/UT is required")
	@Size(max = 100)
	@Column(name = "state_ut", nullable = false, length = 100)
	private String stateUT;
}
