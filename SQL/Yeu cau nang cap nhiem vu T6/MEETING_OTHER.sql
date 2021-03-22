--------------------------------------------------------
--  DDL for Table MEETING_OTHER on DB2
--------------------------------------------------------
CREATE TABLE MEETING_OTHER 
(
  ID NUMBER(22, 0) NOT NULL 
, TITLE NVARCHAR2(2000) 
, SUMARY NVARCHAR2(2000)
, START_TIME TIMESTAMP(6)
, CREATED_BY NUMBER(22,0)
, CREATED_DATE DATE
, UPDATED_BY NUMBER(22,0)
, UPDATED_DATE DATE
, DELETED_BY NUMBER(22,0)
, DELETED_DATE DATE
, DEL_FLAG NUMBER(1,0)
, END_TIME TIMESTAMP(6)
, TEXT_ID NUMBER(22,0)
, "ORDER" NUMBER(10,0)
, CONSTRAINT MEETING_OTHER_PK PRIMARY KEY 
  (
    ID 
  )
  USING INDEX 
  (
      CREATE UNIQUE INDEX MEETING_OTHER_PK ON MEETING_OTHER (ID ASC)
  )
  ENABLE 
);

COMMENT ON COLUMN MEETING_OTHER.ID IS 'ID Meeting Other';

COMMENT ON COLUMN MEETING_OTHER.TITLE IS 'Tieu de lich hop';

COMMENT ON COLUMN MEETING_OTHER.SUMARY IS 'Noi dung mo ta lich hop';

COMMENT ON COLUMN MEETING_OTHER.START_TIME IS 'thoi gian bat dau cuoc hop';

COMMENT ON COLUMN MEETING_OTHER.CREATED_BY IS 'Nguoi tao';

COMMENT ON COLUMN MEETING_OTHER.CREATED_DATE IS 'Ngay tao';

COMMENT ON COLUMN MEETING_OTHER.UPDATED_BY IS 'Nguoi sua';

COMMENT ON COLUMN MEETING_OTHER.UPDATED_DATE IS 'Ngay sua';

COMMENT ON COLUMN MEETING_OTHER.DELETED_BY IS 'Nguoi xoa';

COMMENT ON COLUMN MEETING_OTHER.DELETED_DATE IS 'Ngay xoa';

COMMENT ON COLUMN MEETING_OTHER.DEL_FLAG IS 'Trạng thái: 0-Default, Da xoa';

COMMENT ON COLUMN MEETING_OTHER.END_TIME IS 'Thoi gian ket thuc cuoc hop';

COMMENT ON COLUMN MEETING_OTHER.TEXT_ID IS 'ID van ban';


--------------------------------------------------------
--  DDL for Sequence MEETING_OTHER_SEQ
--------------------------------------------------------

CREATE SEQUENCE  "MEETING_OTHER_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

