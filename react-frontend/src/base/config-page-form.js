import React, {useEffect} from 'react';
import Spinner from "react-bootstrap/Spinner";
import {Alert} from "react-bootstrap";
import {GoFlame, GoThumbsup} from "react-icons/go";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import Form from "react-bootstrap/Form";
import bsCustomFileInput from "bs-custom-file-input";

class FormInputField extends React.Component {

    constructor(props) {
        super(props);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    bubbleEvent(e) {
        this.props.onChange(e.target.name, e.target.value);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.value !== this.props.value) {
            this.props.onChange(this.props.name, this.props.value);
        }
    }

    render() {
        let controlId = this.props.name;
        let label = this.props.label === undefined ? null : <Form.Label column sm={2}>
            {this.props.label}
        </Form.Label>;
        let inputFieldType = this.props.type === undefined ? "text" : this.props.type;
        let inputFieldName = this.props.name;
        let inputFieldPlaceholder = this.props.placeholder === undefined ? this.props.name : this.props.placeholder;
        let inputFieldErrorMessages = this.props.onError(this.props.name);

        return (
            <Form.Group as={Row} controlId={controlId}>
                {label}
                <Col sm={10}>
                    <Form.Control type={inputFieldType}
                                  name={inputFieldName}
                                  placeholder={inputFieldPlaceholder}
                                  onChange={this.bubbleEvent}
                                  value={this.props.value} />

                    <ErrorMessageList fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

function FormFileField(props) {

    useEffect(() => {
        bsCustomFileInput.init();
        return () => {
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

                <ErrorMessageList fieldErrors={inputFieldErrorMessages} />
            </Col>
        </Form.Group>
    );
}

class FormSelectField extends React.Component {

    constructor(props) {
        super(props);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.options !== this.props.options && this.props.options.length > 0) {
            this.props.onChange(this.props.name, this.props.options[0]);
        }
    }

    bubbleEvent(e) {
        this.props.onChange(e.target.name, e.target.value);
    }

    render() {
        let labelText = this.props.label === undefined ? this.props.name : this.props.label;
        let inputFieldErrorMessages = this.props.onError[this.props.name];
        let inputFieldOptions = this.props.options === undefined ? null :
            this.props.options.map((value) => {
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

                    <ErrorMessageList fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}


function ErrorMessageList(props) {

    let doNotRenderComponent = props.fieldErrors === undefined || props.fieldErrors === null;

    if (doNotRenderComponent) {
        return null;
    }

    let backgroundClass = props.backgroundClass === undefined ? "bg-danger" : props.backgroundClass;

    return (
        <div>
            <ul className="error-list">
                {props.fieldErrors.map((message, index) =>
                    <ErrorListItem key={index} backgroundClass={backgroundClass} message={message} />)}
            </ul>
        </div>
    );
}

function ErrorListItem(props) {
    return (
        <li className="error-list-item">
            <Form.Text className={props.backgroundClass + " error"}>
                <GoFlame /> {props.message}
            </Form.Text>
        </li>
    );
}

export default class ConfigPageForm extends React.Component {

    constructor(props) {
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

    startSpinner() {
        this.setState({showSpinner: true});
    }

    stopSpinner() {
        this.setState({showSpinner: false});
    }

    setInputFieldErrors(errors) {
        if (errors !== undefined && errors !== null && typeof errors === 'object') {
            this.setState({inputFieldErrors: errors})
        } else {
            this.setState({inputFieldErrors: undefined})
        }
    }

    getInputFieldErrors(name) {
        if (this.state.inputFieldErrors && this.state.inputFieldErrors[name] &&
            this.state.inputFieldErrors[name] !== null) {
            return this.state.inputFieldErrors[name];
        }
    }

    handleChange(fieldName, value) {
        let staleState = this.state;
        staleState["inputFields"][fieldName] = value;
        this.setState(staleState);
    }

    getFormData() {
        let data = new FormData();
        for (let [key, value] of Object.entries(this.state.inputFields)) {
            data.append(key, value);
        }
        return data;
    }

    handleSubmit(e) {
        e.preventDefault();

        let data = this.getFormData();
        let httpMethod = this.props.httpMethod === undefined ? "GET" : this.props.httpMethod;

        this.startSpinner();

        fetch(this.props.submitUrl, {
            method: httpMethod,
            body: data
        })
            .then((response) => {
                this.stopSpinner()
                if (response.status >= 200 && response.status <= 399) {
                    response.json().then(json => {
                        this.onSuccessResponse(response.status, json)
                    });
                } else {
                    response.json().then(json => {
                        this.onErrorResponse(response.status, json);
                    });
                }
            });
    }

    onSuccessResponse(status, response) {
        this.setState({success: true, inputFieldErrors: {}, errorMessages: undefined});
        if (this.props.onSubmitSuccess !== undefined) {
            this.props.onSubmitSuccess(status, response);
        }
    }

    onErrorResponse(status, response) {
        if (response.inputFieldErrors !== undefined) {
            this.setInputFieldErrors(response.inputFieldErrors);
        } else if (response.errorMessages !== undefined && response.errorMessages !== null &&
            response.errorMessages.length > 0) {
            this.setState({
                errorMessages: response.errorMessages,
                inputFieldErrors: {}
            });
        } else {
            let errorMessages = ["[Unexpected error]"];
            Object.keys(response).forEach(function (key) {
                let value = String(response[key]);
                if (value !== undefined && value != null && value.length > 0) {
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

    render() {
        let className = this.props.disabled === undefined || this.props.disabled === false ? "" : "disabled";
        let spinner;
        if (this.state.showSpinner) {
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
    FormSelectField
}
