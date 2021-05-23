import React, {useEffect} from 'react';
import Spinner from "react-bootstrap/Spinner";
import {Alert} from "react-bootstrap";
import {GoFlame, GoThumbsup} from "react-icons/go";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import bsCustomFileInput from "bs-custom-file-input";
import {Optional} from "../services/utils";

/**
 * a simple input field that might also display error messages directly bound to this input field
 */
class FormInputField extends React.Component
{

    constructor(props)
    {
        super(props);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    bubbleEvent(e)
    {
        if (this.props.type === 'number')
        {
            if (!isNaN(parseInt(e.target.value)))
            {
                this.props.onChange(e.target.name, e.target.valueAsNumber);
            }
            else
            {
                this.props.onChange(e.target.name, undefined);
            }
        }
        else
        {
            this.props.onChange(e.target.name, e.target.value);
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.value !== this.props.value)
        {
            this.props.onChange(this.props.name, this.props.value);
        }
    }

    render()
    {
        let controlId = this.props.name;
        let label = this.props.label === undefined ? null : <Form.Label column sm={2}>
            {this.props.label}
        </Form.Label>;
        let inputFieldType = this.props.type === undefined ? "text" : this.props.type;
        let inputFieldName = this.props.name;
        let inputFieldPlaceholder = this.props.placeholder === undefined ? this.props.name : this.props.placeholder;
        let inputFieldErrorMessages = this.props.onError(this.props.name);
        let isDisabled = this.props.disabled === true;
        let isReadOnly = this.props.readOnly === true;

        return (
            <Form.Group as={Row} controlId={controlId}>
                {label}
                <Col sm={10}>
                    <Form.Control type={inputFieldType}
                                  name={inputFieldName}
                                  disabled={isDisabled}
                                  readOnly={isReadOnly}
                                  placeholder={inputFieldPlaceholder}
                                  onChange={this.bubbleEvent}
                                  value={this.props.value} />

                    <ErrorMessageList controlId={this.props.name + "-error-list"}
                                      fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

/**
 * a file-input field that might also display error messages directly bound to this input field
 */
function FormFileField(props)
{

    useEffect(() =>
    {
        bsCustomFileInput.init();
        return () =>
        {
            bsCustomFileInput.destroy();
        }
    }, [] /* do this only once */)

    let labelText = props.label === undefined ? props.name : props.label;
    let inputFieldPlaceholder = props.placeholder === undefined ? "Select a file" : props.placeholder;
    let inputFieldButtonText = props.button === undefined ? "Search" : props.button;
    let inputFieldErrorMessages = props.onError(props.name);

    return (
        <Form.Group as={Row} controlId={props.name}>
            <Form.Label column sm={2}>{labelText}</Form.Label>
            <Col sm={10}>
                <Form.File custom>
                    <Form.File.Input isValid
                                     name={props.name}
                                     onChange={e => props.onChange(e.target.name, e.target.files[0])} />
                    <Form.File.Label data-browse={inputFieldButtonText}>
                        {inputFieldPlaceholder}
                    </Form.File.Label>
                </Form.File>

                <ErrorMessageList controlId={props.name + "-error-list"}
                                  fieldErrors={inputFieldErrorMessages} />
            </Col>
        </Form.Group>
    );
}

/**
 * a select input field that might also display error messages directly bound to this input field
 */
class FormSelectField extends React.Component
{

    constructor(props)
    {
        super(props);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    componentDidMount()
    {
        if (this.props.options !== undefined && this.props.options.length > 0)
        {
            this.props.onChange(this.props.name, this.props.options[0]);
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot)
    {
        if (prevProps.options !== this.props.options && this.props.options.length > 0)
        {
            this.props.onChange(this.props.name, this.props.options[0]);
        }
    }

    bubbleEvent(e)
    {
        this.props.onChange(e.target.name, e.target.value);
    }

    render()
    {
        let labelText = this.props.label === undefined ? this.props.name : this.props.label;
        let inputFieldErrorMessages = this.props.onError(this.props.name);
        let inputFieldOptions = this.props.options === undefined ? null :
                                this.props.options.map((value) =>
                                {
                                    return <option key={value}>{value}</option>
                                });

        return (
            <Form.Group as={Row} controlId={this.props.name}>
                <Form.Label column sm={2}>
                    {labelText}
                </Form.Label>
                <Col sm={10}>
                    <Form.Control as="select"
                                  name={this.props.name}
                                  onChange={this.bubbleEvent}>
                        {inputFieldOptions}
                    </Form.Control>

                    <ErrorMessageList controlId={this.props.name + "-error-list"}
                                      fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

/**
 * displays error messages for a {@link ConfigPageForm} element
 */
function ErrorMessageList(props)
{
    let doNotRenderComponent = new Optional(props.fieldErrors).map(val => false).orElse(true);

    if (doNotRenderComponent)
    {
        return null;
    }

    let backgroundClass = props.backgroundClass === undefined ? "bg-danger" : props.backgroundClass;

    return (
        <ul id={props.controlId} className="error-list">
            {props.fieldErrors.map((message, index) =>
                <ErrorListItem key={index} backgroundClass={backgroundClass} message={message} />)}
        </ul>
    );
}

/**
 * a simple error message for either the {@link ErrorMessageList} or an error that is directly bound to an
 * input field
 */
function ErrorListItem(props)
{
    return (
        <li className="error-list-item">
            <Form.Text className={props.backgroundClass + " error"}>
                <GoFlame /> {props.message}
            </Form.Text>
        </li>
    );
}

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
export default class ConfigPageForm extends React.Component
{

    constructor(props)
    {
        super(props);
        this.state = {showSpinner: false, inputFields: {}, inputFieldErrors: {}};
        this.startSpinner = this.startSpinner.bind(this);
        this.stopSpinner = this.stopSpinner.bind(this);
        this.handleChange = this.handleChange.bind(this);
        this.setInputFieldErrors = this.setInputFieldErrors.bind(this);
        this.getInputFieldErrors = this.getInputFieldErrors.bind(this);
        this.onSuccessResponse = this.onSuccessResponse.bind(this);
        this.onErrorResponse = this.onErrorResponse.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    /**
     * starts the spinner on the submit button
     */
    startSpinner()
    {
        this.setState({showSpinner: true});
    }

    /**
     * stops the spinner on the submit button
     */
    stopSpinner()
    {
        this.setState({showSpinner: false});
    }

    /**
     * adds input field errors to the current state
     */
    setInputFieldErrors(errors)
    {
        if (errors !== undefined && errors !== null && typeof errors === 'object')
        {
            this.setState({inputFieldErrors: errors, errorMessages: undefined})
        }
        else
        {
            this.setState({inputFieldErrors: undefined, errorMessages: undefined})
        }
    }

    /**
     * returns the input field errors for the underlying input-field components
     */
    getInputFieldErrors(name)
    {
        if (this.state.inputFieldErrors && this.state.inputFieldErrors[name] &&
            this.state.inputFieldErrors[name] !== null)
        {
            return this.state.inputFieldErrors[name];
        }
    }

    /**
     * this method is passed to the underlying components as the "onChange"-method
     * @param fieldName name of the input field
     * @param value value of the input field
     */
    handleChange(fieldName, value)
    {
        let staleState = this.state;
        if (value === undefined)
        {
            delete staleState["inputFields"][fieldName];
        }
        else
        {
            staleState["inputFields"][fieldName] = value;
        }
        this.setState(staleState);
    }

    /**
     * the method {@link #handleChange} adds the current values into the state context under the key "inputFields".
     * This method will read this data and add it to the coming request to the backend
     */
    getFormData()
    {
        let data = new FormData();
        for (let [key, value] of Object.entries(this.state.inputFields))
        {
            data.append(key, value);
        }
        return data;
    }

    /**
     * send the request to the backend
     */
    handleSubmit(e)
    {
        e.preventDefault();

        // get the data to be send in the request body
        let data = this.getFormData();
        // if no method was given expect a simple GET request
        let httpMethod = this.props.httpMethod === undefined ? "GET" : this.props.httpMethod;

        // start the spinner in the button
        this.startSpinner();

        fetch(this.props.submitUrl, {
            method: httpMethod,
            body: data
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
     * the parent component must add a function within the props that will be executed on successful request since
     * this form has no knowledge of how to handle a successful response
     */
    onSuccessResponse(status, response)
    {
        this.setState({success: true, inputFieldErrors: {}, errorMessages: undefined});
        if (this.props.onSubmitSuccess !== undefined)
        {
            this.props.onSubmitSuccess(status, response);
        }
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

    render()
    {
        let className = this.props.disabled === undefined || this.props.disabled === false ? "" : "disabled";
        let spinner;
        if (this.state.showSpinner)
        {
            spinner = <span style={{marginRight: 5 + 'px'}}>
                          <Spinner animation="border" variant="warning" size="sm" role="status" />
                      </span>;
        }

        let buttonText = this.props.buttonText === undefined ? "Submit" : this.props.buttonText;

        return (
            <Form id={this.props.formId} className={className} onSubmit={this.handleSubmit}>
                <h2>{this.props.header}</h2>

                <Alert id={this.props.formId + "-alert-success"} variant={"success"} show={this.state.success === true}>
                    <Form.Text>
                        <GoThumbsup /> {this.props.successMessage}
                    </Form.Text>
                </Alert>

                <Alert id={this.props.formId + "-alert-error"} variant={"danger"}
                       show={this.state.errorMessages !== undefined}>
                    <ErrorMessageList fieldErrors={this.state.errorMessages} backgroundClass={""} />
                </Alert>

                {this.props.children({
                    onChange: this.handleChange,
                    onError: this.getInputFieldErrors
                })}

                <Form.Group as={Row}>
                    <Col sm={{span: 10, offset: 2}}>
                        <Button id={this.props.buttonId} type="submit" onClick={() => this.startSpinner}>
                            {spinner} {buttonText}
                        </Button>
                        {
                            this.props.additionalButtons !== undefined &&
                            this.props.additionalButtons.map(button =>
                            {
                                return button
                            })
                        }

                    </Col>
                </Form.Group>
            </Form>
        );
    }
}

export {
    ConfigPageForm,
    FormInputField,
    FormFileField,
    FormSelectField,
    ErrorMessageList,
    ErrorListItem
}
