
-- db2 
-- can check xem co trung seq tren db test
INSERT INTO SYSTEM_PARAMETER ( SYSTEM_PARAMETER_ID, CODE, NAME, VALUE, DESCRIPTION )
VALUES( SYSTEM_PARAMETER_SEQ.NEXTVAL, 'SYS_PARAM_PER_DOC', 'Cấu hình cho lưu trữ cá nhân', '{"1": "Văn bản", "2": "Lịch họp"}', '1' );

INSERT INTO SYSTEM_PARAMETER ( SYSTEM_PARAMETER_ID, CODE, NAME, VALUE, DESCRIPTION )
VALUES(SYSTEM_PARAMETER_SEQ.NEXTVAL, 'SYS_PARAM_PER_DOC_DETAIL_DOC', 'Cấu hình cho lưu trữ cá nhân tìm kiếm theo văn bản', '{"1": "TO", "2": "CC"}', '2' );


INSERT INTO SYSTEM_PARAMETER ( SYSTEM_PARAMETER_ID, CODE, NAME, VALUE, DESCRIPTION )
VALUES(SYSTEM_PARAMETER_SEQ.NEXTVAL, 'SYS_PARAM_PER_DOC_DETAIL_MEET', 'Cấu hình cho lưu trữ cá nhân tìm kiếm theo lịch họp', '{"1": "tiêu đề", "2" :"nội dung ghi chú", "3": "nội dung gợi nhớ"}', '3' );