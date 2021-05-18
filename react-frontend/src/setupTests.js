// jest-dom adds custom jest matchers for asserting on DOM nodes.
// allows you to do things like:
// expect(element).toHaveTextContent(/react/i)
// learn more: https://github.com/testing-library/jest-dom
import '@testing-library/jest-dom';
import {fireEvent, waitFor} from "@testing-library/react";

export function mockFetch(status, fakeResponse) {
    jest.spyOn(global, "fetch").mockImplementation(() => {
        return Promise.resolve({
            status: status,
            json: () => {
                return Promise.resolve(fakeResponse)
            }
        })
    });
}

export default class Assertions {

    querySelector;
    element;

    constructor(querySelector) {
        this.querySelector = querySelector;
        this.element = document.querySelector(this.querySelector);
    }

    findClosest(querySelector) {
        this.element.closest(querySelector);
    }

    isPresent() {
        expect(this.element).toBeInTheDocument();
        return this;
    }

    isVisible() {
        expect(this.element).toBeVisible();
        return this;
    }

    isNotPresent() {
        expect(this.element).not.toBeInTheDocument();
        return this;
    }

    assertEquals(expectedText) {
        expect(this.element.textContent.trim()).toBe(expectedText);
        return this;
    }

    hasClass(className) {
        expect(this.element).toHaveClass(className);
        return this;
    }

    hasNotClass(className) {
        expect(this.element).not.toHaveClass(className);
        return this;
    }

    hasValueSelected(value) {
        expect(this.element.value).toBe(value);
        return this;
    }

    async fireChangeEvent(value) {
        if (value instanceof File) {
            await fireEvent.change(this.element, {target: {files: [value]}});
        } else {
            await fireEvent.change(this.element, {target: {value: value}});
        }
        return this;
    }

    async clickElement(awaitCondition) {
        this.element.dispatchEvent(new MouseEvent('click', {bubbles: true}));
        if (awaitCondition !== undefined) {
            await waitFor(() => {
                awaitCondition();
            });
        }
        return this
    }

    hasErrors(errorMessages) {
        let fieldErrors = this.element.querySelectorAll(".error-list-item");
        expect(fieldErrors.length).toBe(errorMessages.length)

        for (let i = 0; i < errorMessages; i++) {
            const message = errorMessages[i];
            expect(fieldErrors[i].textContent).toBe(message);
        }
    }
}

