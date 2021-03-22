package com.viettel.voffice.controler;

import java.util.HashMap;
import java.util.*;
import org.junit.*;
import javax.servlet.http.HttpServletRequest;
import org.mockito.*;

import com.viettel.voffice.controler.AnswerDocumentController;

import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.*;
import org.hamcrest.core.AnyOf;
import static org.hamcrest.MatcherAssert.assertThat;

/** * Autogen class Test for class: AnswerDocumentController *  * @author ToolGenTest * @date Mon Mar 22 17:11:56 ICT 2021 */public class AnswerDocumentControllerTest {

    @InjectMocks
    AnswerDocumentController service;

    @Before
    public void setUp() {
       service = new AnswerDocumentController();
       MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getListGroupReceiverRequestResponse0() {
        HttpServletRequest arg0 = null;
        String arg1 = null;
        String actualResult = service.getListGroupReceiverRequestResponse(arg0, arg1);
        assertThat(actualResult, AnyOf.anyOf(is(Matchers.nullValue()),is("")));
    }

    @Test
    public void insertDocumentReply0() {
        HttpServletRequest arg0 = null;
        String arg1 = null;
        String actualResult = service.insertDocumentReply(arg0, arg1);
        assertThat(actualResult, AnyOf.anyOf(is(Matchers.nullValue()),is("")));
    }

    @Test
    public void cancelDocumentReply0() {
        HttpServletRequest arg0 = null;
        String arg1 = null;
        String actualResult = service.cancelDocumentReply(arg0, arg1);
        assertThat(actualResult, AnyOf.anyOf(is(Matchers.nullValue()),is("")));
    }

    @Test
    public void getListAnswerDocument0() {
        HttpServletRequest arg0 = null;
        String arg1 = null;
        String actualResult = service.getListAnswerDocument(arg0, arg1);
        assertThat(actualResult, AnyOf.anyOf(is(Matchers.nullValue()),is("")));
    }

    @Test
    public void replyDocument0() {
        HttpServletRequest arg0 = null;
        String arg1 = null;
        String actualResult = service.replyDocument(arg0, arg1);
        assertThat(actualResult, AnyOf.anyOf(is(Matchers.nullValue()),is("")));
    }
}