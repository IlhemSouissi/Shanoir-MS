/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.examination.dto.mapper;

import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;

/**
 * Decorator for examinations mapper.
 * 
 * @author msimon
 *
 */
public abstract class ExaminationDecorator implements ExaminationMapper {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ExaminationDecorator.class);
	
	@Autowired
	private CenterRepository centerRepository;
	
	@Autowired
	private StudyRepository studyRepository;
	
	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private ExaminationMapper delegate;

	@Override
	public PageImpl<ExaminationDTO> examinationsToExaminationDTOs(Page<Examination> page) {

		Page<ExaminationDTO> mappedPage = page.map(new Converter<Examination, ExaminationDTO>() {
			@Override
			public ExaminationDTO convert(Examination entity) {
				return examinationToExaminationDTO(entity);
			}
		});
		return new PageImpl<>(mappedPage);
	}

	@Override
	public ExaminationDTO examinationToExaminationDTO(Examination examination) {
		final ExaminationDTO examinationDTO = delegate.examinationToExaminationDTO(examination);

		if (examination.getCenterId() != null) {
			final Center center = centerRepository.findOne(examination.getCenterId());
			if (center != null) {
				examinationDTO.setCenter(new IdName(examination.getCenterId(), center.getName()));
			}
		}

		if (examination.getStudyId() != null) {
			final Study study = studyRepository.findOne(examination.getStudyId());
			if (study != null) {
				examinationDTO.setStudy(new IdName(examination.getStudyId(), study.getName()));
			}
		}
		
		if (examination.getSubjectId() != null) {
			final Subject subject = subjectRepository.findOne(examination.getSubjectId());
			if (subject != null) {
				examinationDTO.setSubject(new IdName(examination.getSubjectId(), subject.getName()));
			}
		}

		return examinationDTO;
	}

}
