import React from 'react';
import {act, render} from '@testing-library/react';
import {unmountComponentAtNode} from "react-dom";
import ConfigPageForm, {FormFileField, FormInputField, FormSelectField} from "./config-page-form";
import Assertions, {mockFetch} from "../setupTests";

let container = null;

/* ********************************************************************************************************* */

beforeEach(() =>
{
    // setup a DOM element as a render target
    container = document.createElement("div");
    document.body.appendChild(container);
});

/* ********************************************************************************************************* */

afterEach(() =>
{
    // cleanup on exiting
    unmountComponentAtNode(container);
    container.remove();
    container = null;
});

/* ********************************************************************************************************* */

class TestFormBuilder extends React.Component
{

    handleSuccessResponse()
    {
        // do nothing since we are not testing this. This is implicitly tested with other tests
    }

    render()
    {
        const options = ["selection-1", "selection-2"];

        return (
            <ConfigPageForm formId="my-form-id"
                            header="My Form"
                            httpMethod="POST"
                            submitUrl="/test-submit"
                            onSubmitSuccess={this.handleSuccessResponse}
                            buttonId="my-button-id"
                            buttonText="my-special-submit">
                {({onChange, onError}) => (
                    <React.Fragment>
                        <FormFileField name="myFile"
                                       label="a file"
                                       placeholder="Select a file"
                                       onChange={onChange}
                                       onError={onError} />
                        <FormInputField name="myText"
                                        label="a text"
                                        placeholder="any text"
                                        onChange={onChange}
                                        onError={onError} />
                        <FormSelectField name="mySelection"
                                         label="any selection"
                                         onChange={onChange}
                                         onError={onError}
                                         options={options} />
                    </React.Fragment>
                )}
            </ConfigPageForm>
        )
    }
}

/* ********************************************************************************************************* */

test("receive 'errorMessages' from server", async () =>
{

    act(() =>
    {
        render(<TestFormBuilder />, container);
    });

    new Assertions("#my-form-id").isPresent().isVisible();
    new Assertions("#myFile").isPresent().isVisible();
    new Assertions("#myText").isPresent().isVisible();
    new Assertions("#mySelection").isPresent().isVisible();

    const errorMessage1 = "Unexpected exception during isValid call.";
    const errorMessage2 = "NullPointerException";
    const errorMessageResponse = {
        "errorMessages": [
            errorMessage1,
            errorMessage2
        ]
    };

    mockFetch(500, errorMessageResponse);

    let errorMessageAssertion;
    await new Assertions("#my-button-id").isPresent().isVisible().clickElement(() =>
    {
        errorMessageAssertion = new Assertions("#my-form-id-alert-error").isPresent().isVisible();
    })
    expect(global.fetch).toBeCalledTimes(1);
    let data = new FormData();
    data.append("mySelection", "selection-1")
    expect(global.fetch).toBeCalledWith("/test-submit", {
        body: data,
        method: "POST"
    })
    global.fetch.mockRestore();

    let errorMessages = document.querySelectorAll("#my-form-id-alert-error .error-list-item");
    expect(errorMessages.length).toBe(2);
    expect(errorMessages[0].textContent.trim()).toBe(errorMessage1);
    expect(errorMessages[1].textContent.trim()).toBe(errorMessage2);
});

/*
 *********************************************************************************************************
 */

test("receive 'inputFieldErrors' from server", async () =>
{
    act(() =>
    {
        render(<TestFormBuilder />, container);
    });

    new Assertions("#my-form-id").isPresent().isVisible();
    const myFileAssertion = new Assertions("#myFile").isPresent().isVisible();
    const myTextAssertion = new Assertions("#myText").isPresent().isVisible();
    const mySelectionAssertion = new Assertions("#mySelection").isPresent().isVisible();

    const myTextErrorMessage1 = "Only accepting digits";
    const myTextErrorMessage2 = "Maximum number is 6";
    const myTextErrorMessage3 = "Minimum number is 3";

    const myFileErrorMessage1 = "Not accepting empty files";
    const myFileErrorMessage2 = "Only accepting files of type .txt";

    const mySelectionErrorMessage1 = "please select a value";

    const errorMessageResponse = {
        "inputFieldErrors": {
            "myText": [myTextErrorMessage1, myTextErrorMessage2, myTextErrorMessage3],
            "myFile": [myFileErrorMessage1, myFileErrorMessage2],
            "mySelection": [mySelectionErrorMessage1]
        }
    };
    mockFetch(500, errorMessageResponse);

    myTextAssertion.fireChangeEvent("4-6");
    const fileToUpload = new File(["blubb"], "blubb.cmd");
    myFileAssertion.fireChangeEvent(fileToUpload);
    mySelectionAssertion.fireChangeEvent("selection-2");

    let myTextErrorsAssertion;
    let myFileErrorsAssertion;
    let mySelectionErrorsAssertion;
    await new Assertions("#my-button-id").isPresent().isVisible().clickElement(() =>
    {
        myTextErrorsAssertion = new Assertions("#myText-error-list").isPresent().isVisible();
        myFileErrorsAssertion = new Assertions("#myFile-error-list").isPresent().isVisible();
        mySelectionErrorsAssertion = new Assertions("#mySelection-error-list").isPresent().isVisible();
    })
    expect(global.fetch).toBeCalledTimes(1);
    let data = new FormData();
    data.append("mySelection", "selection-2")
    data.append("myText", "4-6")
    data.append("myFile", fileToUpload)
    expect(global.fetch).toBeCalledWith("/test-submit", {
        body: data,
        method: "POST"
    })
    global.fetch.mockRestore();

    myTextErrorsAssertion.hasErrors([myTextErrorMessage1, myTextErrorMessage2, myTextErrorMessage3]);
    myFileErrorsAssertion.hasErrors([myFileErrorMessage1, myFileErrorMessage2]);
    mySelectionErrorsAssertion.hasErrors([mySelectionErrorMessage1]);
});

/*
 *********************************************************************************************************
 */
