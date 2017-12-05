package org.shanoir.ng.study;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.utils.SecurityContextTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Study mapper test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class StudyMapperTest {

	private static final Long STUDY_ID = 1L;
	private static final String STUDY_NAME = "test";

	@Autowired
	private StudyMapper studyMapper;

	@Test
	public void studiesToStudyDTOsTest() {
		final List<StudyDTO> studyDTOs = studyMapper.studiesToStudyDTOs(Arrays.asList(createStudy()));
		Assert.assertNotNull(studyDTOs);
		Assert.assertTrue(studyDTOs.size() == 1);
		Assert.assertTrue(studyDTOs.get(0).getId().equals(STUDY_ID));
	}

	@Test
	public void studyToStudyDTOTest() {
		SecurityContextTestUtil.initAuthenticationContext();
		
		final StudyDTO studyDTO = studyMapper.studyToStudyDTO(createStudy());
		Assert.assertNotNull(studyDTO);
		Assert.assertTrue(studyDTO.getId().equals(STUDY_ID));
	}

	private Study createStudy() {
		final Study study = new Study();
		study.setId(STUDY_ID);
		study.setName(STUDY_NAME);
		return study;
	}

}