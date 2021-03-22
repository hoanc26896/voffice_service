--------------------------------------------------------
--  DDL for Table DOCUMENT_REQUEST_REPLY
--------------------------------------------------------
CREATE TABLE DOCUMENT_REQUEST_REPLY 
(
  DOCUMENT_REQUEST_REPLY_ID NUMBER(30, 0) NOT NULL 
, DOCUMENT_ID NUMBER(30, 0) 
, VHR_ORG_ID NUMBER(30, 0) 
, EXPIRED_DATE DATE 
, CREATE_DATE DATE 
, UPDATE_DATE DATE 
, STAFF_ID_VOF2 NUMBER(30, 0) 
, STATUS NUMBER(1, 0) 
, DOCUMENT_REPLY_ID NVARCHAR2(2000) 
, DEL_FLAG NUMBER(1, 0) 
, REPLY_ID_VOF2 NUMBER(30, 0) 
, CONSTRAINT DOCUMENT_REQUEST_REPLY_PK PRIMARY KEY 
  (
    DOCUMENT_REQUEST_REPLY_ID 
  )
  USING INDEX 
  (
      CREATE UNIQUE INDEX DOCUMENT_REQUEST_REPLY_PK ON DOCUMENT_REQUEST_REPLY (DOCUMENT_REQUEST_REPLY_ID ASC)
  )
  ENABLE 
);

CREATE INDEX DOCUMENT_REQUEST_REPLY_INDEX1 ON DOCUMENT_REQUEST_REPLY (DOCUMENT_ID ASC) ;

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.DOCUMENT_ID IS 'ID văn bản';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.VHR_ORG_ID IS 'ID đơn vị yêu cầu trả lời';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.EXPIRED_DATE IS 'Hạn trả lời';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.CREATE_DATE IS 'Ngày yêu cầu';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.UPDATE_DATE IS 'Ngày cập nhật';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.STAFF_ID_VOF2 IS 'ID người yêu cầu';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.STATUS IS 'Trạng thái: 0-Chưa trả lời, Đã trả lời';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.DOCUMENT_REPLY_ID IS 'ID văn bản trả lời';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.DEL_FLAG IS '0- Chưa xóa, 1-Đã xóa';

COMMENT ON COLUMN DOCUMENT_REQUEST_REPLY.REPLY_ID_VOF2 IS 'ID người trả lời';

--------------------------------------------------------
--  DDL for Sequence DOCUMENT_REQUEST_REPLY_SEQ
--------------------------------------------------------

CREATE SEQUENCE  "DOCUMENT_REQUEST_REPLY_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER  NOCYCLE ;

--------------------------------------------------------
--  DDL for Type T_TABLE
--------------------------------------------------------
create or replace TYPE T_TABLE IS OBJECT
(
    ID int
    , VALUE VARCHAR2(2000)
);

--------------------------------------------------------
--  DDL for Type T_TABLE_COLL
--------------------------------------------------------
create or replace TYPE T_TABLE_COLL IS TABLE OF T_TABLE;

--------------------------------------------------------
--  DDL for FUNCTION F_TABLE_SPLIT
--------------------------------------------------------
CREATE OR REPLACE FUNCTION F_TABLE_SPLIT(
    i_query     VARCHAR2,
    i_seperator VARCHAR2 DEFAULT ',')
  RETURN T_TABLE_COLL
IS
  l_res_coll T_TABLE_COLL;
  l_index NUMBER;
BEGIN
  l_res_coll := T_TABLE_COLL();
  FOR i IN
  ( WITH TAB AS
  ( SELECT '1' ID, i_query STR FROM DUAL
  )
SELECT id,
  SUBSTR(STR, instr(STR, i_seperator, 1, lvl) + 1, instr(STR, i_seperator, 1, lvl + 1) - instr(STR, i_seperator, 1, lvl) - 1) name
FROM
  ( SELECT i_seperator || STR || i_seperator AS STR, id FROM TAB
  ),
  ( SELECT level AS lvl FROM dual CONNECT BY level <= 100
  )
WHERE lvl <= LENGTH(STR) - LENGTH(REPLACE(STR, i_seperator)) - 1
ORDER BY ID,
  NAME
  )
  LOOP
    --IF i.ID = 1001 THEN
    l_res_coll.extend;
    l_index            := l_res_coll.count;
    l_res_coll(l_index):= T_TABLE(i.ID, i.name);
    --END IF;
  END LOOP;
RETURN l_res_coll;
END;

--------------------------------------------------------
--  DDL for FUNCTION F_NAME_DOCUMENT
--------------------------------------------------------
create or replace FUNCTION "F_NAME_DOCUMENT"(
    str_input VARCHAR2)
  RETURN VARCHAR2
IS
  tmp NVARCHAR2(2000) DEFAULT '';
  CURSOR strName
  IS
    SELECT LISTAGG(concat(concat('- ', concat(concat(d.code,'_'),d.TITLE)),chr(10))) WITHIN GROUP (
    ORDER BY d.code, d.TITLE) as nameDoc
    FROM document d
    WHERE d.DOCUMENT_ID IN
      (SELECT value FROM TABLE(F_TABLE_SPLIT(str_input))
      );
BEGIN
  tmp := '';
  FOR p_strName IN strName
  LOOP
    tmp := p_strName.nameDoc;
  END LOOP;
  return tmp;
END;

--------------------------------------------------------
--  DDL for TABLE DOCUMENT_OFFICE_SEND
--------------------------------------------------------

CREATE TABLE DOCUMENT_OFFICE_SEND 
(
  DOCUMENT_OFFICE_SEND_ID NUMBER(30, 0) NOT NULL 
, OFFICE_NAME NVARCHAR2(2000) 
, DEL_FLAG NUMBER(1, 0) 
, CREATE_ID NUMBER(30, 0) 
, VHR_ORG_CREATE_ID NUMBER(30, 0) 
, CREATE_DATE DATE 
, TYPE NUMBER(1, 0) 
, CONSTRAINT DOCUMENT_OFFICE_SEND_PK PRIMARY KEY 
  (
    DOCUMENT_OFFICE_SEND_ID 
  )
  USING INDEX 
  (
      CREATE UNIQUE INDEX DOCUMENT_OFFICE_SEND_PK ON DOCUMENT_OFFICE_SEND (DOCUMENT_OFFICE_SEND_ID ASC)
  )
  ENABLE 
)
);

COMMENT ON COLUMN DOCUMENT_OFFICE_SEND.OFFICE_NAME IS 'Tên đơn vị khác';

COMMENT ON COLUMN DOCUMENT_OFFICE_SEND.DEL_FLAG IS '0: Hoạt động, 1: đã xóa';

COMMENT ON COLUMN DOCUMENT_OFFICE_SEND.CREATE_ID IS 'ID người tạo';

COMMENT ON COLUMN DOCUMENT_OFFICE_SEND.VHR_ORG_CREATE_ID IS 'ID đơn vị người tạo';

COMMENT ON COLUMN DOCUMENT_OFFICE_SEND.CREATE_DATE IS 'Ngày tạo';

COMMENT ON COLUMN DOCUMENT_OFFICE_SEND.TYPE IS '1: Dùng chung, 2: cá nhân';

Insert into DOCUMENT_OFFICE_SEND (DOCUMENT_OFFICE_SEND_ID,OFFICE_NAME,DEL_FLAG,CREATE_ID,VHR_ORG_CREATE_ID,CREATE_DATE,TYPE) values (1,'Chính phủ',0,null,null,null,1);

Insert into DOCUMENT_OFFICE_SEND (DOCUMENT_OFFICE_SEND_ID,OFFICE_NAME,DEL_FLAG,CREATE_ID,VHR_ORG_CREATE_ID,CREATE_DATE,TYPE) values (2,'Bộ quốc phòng',0,null,null,null,1);

--------------------------------------------------------
--  DDL for Sequence DOCUMENT_OFFICE_SEND_SEQ
--------------------------------------------------------

CREATE SEQUENCE  "DOCUMENT_OFFICE_SEND_SEQ"  MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 3 CACHE 20 NOORDER  NOCYCLE ;

--------------------------------------------------------
--  DDL for TABLE DOCUMENT
--------------------------------------------------------
ALTER TABLE DOCUMENT 
ADD (OFFICE_TYPE NUMBER(2) );

COMMENT ON COLUMN DOCUMENT.OFFICE_TYPE IS '1: Văn bản Chính phủ; 2:Văn bản Quốc phòng; 	null: Văn bản khác 2 loại trên';

--------------------------------------------------------
--  DDL for FILES_ATTACHMENT DOCUMENT
--------------------------------------------------------
ALTER TABLE FILES_ATTACHMENT 
ADD (TYPE NUMBER(2) );

COMMENT ON COLUMN FILES_ATTACHMENT.TYPE IS '1 - File ky chinh; 2 - File dinh kem';
