import ConfigPageForm from "./config-page-form";
import {Optional, toBase64} from "../services/utils";

/**
 * a basic form element that is used to handle communication with the backend. The protocol has been negotiated
 * in advance so the communication can be handled for each form in the same manner
 *
 * @example: unexpected error messages
 * {
 *     "errorMessages": ["An internal error has occurred"]
 * }
 *
 * @example: field error messages
 * {
 *     "inputFieldErrors": {
 *         "keystoreFile": ["File must not be empty", "Keystore was tampered with or password was incorrect"],
 *         "keystorePassword": ["keystore could not be opened"]
 *     }
 * }
 *
 */
export default class ScimConfigPageForm extends ConfigPageForm
{
    /**
     * the method {@link #handleChange} adds the current values into the state context under the key "inputFields".
     * This method will read this data and add it to the coming request to the backend
     */
    async getFormData()
    {
        let addField = function (jsonObject, objectName)
        {
            if (jsonObject[objectName] === undefined || jsonObject[objectName] === null)
            {
                return jsonObject[objectName] = {};
            }
            return jsonObject[objectName];
        }

        let scimResource = {};

        for (let [key, value] of Object.entries(this.state.inputFields))
        {

            let parts = key.split(".")
            let currentObject = scimResource;
            for (let i = 0; i < parts.length - 1; i++)
            {
                currentObject = addField(currentObject, parts[i]);
            }
            if (typeof value.name == 'string')
            {
                currentObject[parts[parts.length - 1]] = await toBase64(value);
            }
            else
            {
                currentObject[parts[parts.length - 1]] = value;
            }
        }

        return scimResource;
    }

    /**
     * send the request to the backend
     */
    async handleSubmit(e)
    {
        e.preventDefault();

        // get the data to be send in the request body
        let jsonData = await this.getFormData();
        // if no method was given expect a simple GET request
        let httpMethod = this.props.httpMethod === undefined ? "GET" : this.props.httpMethod;

        // start the spinner in the button
        this.startSpinner();

        fetch(this.props.submitUrl, {
            method: httpMethod,
            headers: {'Content-Type': 'application/scim+json'},
            body: JSON.stringify(jsonData)
        })
            .then((response) =>
            {
                // stop the spinner in the button
                this.stopSpinner()
                // if the request was successful
                if (response.status >= 200 && response.status <= 399)
                {
                    // this might still cause an error if the response is not json
                    response.json().then(json =>
                    {
                        this.onSuccessResponse(response.status, json)
                    });
                }
                // if the request failed
                else
                {
                    // this might still cause an error if the response is not json
                    response.json().then(json =>
                    {
                        this.onErrorResponse(response.status, json);
                    });
                }
            });
    }

    /**
     * on an error response the form expects to receive a json structure with error messages directly bound to the
     * input fields or an unhandled error message.
     * @see ConfigPageForm ConfigPageForm doc for more information
     */
    onErrorResponse(status, response)
    {
        if (response.errors === undefined || response.errors === null)
        {
            let detail = new Optional(response.detail).map(val => [val]).orElse([]);
            this.setState({
                errorMessages: detail,
                inputFieldErrors: {}
            });
            return;
        }
        if (response.errors.fieldErrors !== undefined)
        {
            this.setInputFieldErrors(response.errors.fieldErrors);
        }
        else if (response.errors.errorMessages !== undefined && response.errors.errorMessages !== null &&
                 response.errors.errorMessages.length > 0)
        {
            this.setState({
                errorMessages: response.errors.errorMessages,
                inputFieldErrors: {}
            });
        }
        else
        {
            let errorMessages = ["[Unexpected error]"];
            Object.keys(response).forEach(function (key)
            {
                let value = String(response[key]);
                if (value !== undefined && value != null && value.length > 0)
                {
                    errorMessages.push(key + ": " + value);
                }
            });
            this.setState({
                errorMessages: errorMessages,
                inputFieldErrors: {}
            });
        }
        this.setState({success: false})
    }
}

export {
    ScimConfigPageForm
}
