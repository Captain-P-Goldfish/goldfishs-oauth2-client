import "./formInputfield.css"
import Row from "react-bootstrap/Row";
import Form from "react-bootstrap/Form";
import Col from "react-bootstrap/Col";
import React from "react";
import {GoFlame} from 'react-icons/go';
import bsCustomFileInput from 'bs-custom-file-input'


class FormInputfield extends React.Component {

    constructor(props) {
        super(props);
        this.onChange = this.onChange.bind(this);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    onChange(e) {
        this.bubbleEvent(e.target.name, e.target.value)
    }

    bubbleEvent(fieldName, fieldValue) {
        this.props.handleFieldChange(fieldName, fieldValue)
    }

    componentDidMount() {
        if (this.props.value !== undefined) {
            this.bubbleEvent(this.props.name, this.props.value)
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
        let inputFieldErrorMessages = this.props.fieldErrors[this.props.name];

        return (
            <Form.Group as={Row} controlId={controlId}>
                {label}
                <Col sm={10}>
                    <Form.Control type={inputFieldType}
                                  name={inputFieldName}
                                  placeholder={inputFieldPlaceholder}
                                  onChange={this.onChange}
                                  value={this.props.value} />

                    <ErrorMessageList fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

class FormSelectfield extends React.Component {

    constructor(props) {
        super(props);
        this.onChange = this.onChange.bind(this);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    onChange(e) {
        this.bubbleEvent(e.target.name, e.target.value)
    }

    bubbleEvent(fieldName, fieldValue) {
        this.props.handleFieldChange(fieldName, fieldValue)
    }

    componentDidMount() {
        if (this.props.options !== undefined && this.props.options !== null && this.props.options.length > 0) {
            this.bubbleEvent(this.props.name, [this.props.options[0]]);
        }
    }

    render() {
        let controlId = this.props.name;
        let labelText = this.props.label === undefined ? this.props.name : this.props.label;
        let inputFieldName = this.props.name;
        let inputFieldErrorMessages = this.props.fieldErrors[this.props.name];
        let inputFieldOptions = this.props.options === undefined ? null :
            this.props.options.map((value) => {
                return <option key={value}>{value}</option>
            });

        return (
            <Form.Group as={Row} controlId={controlId}>
                <Form.Label column sm={2}>
                    {labelText}
                </Form.Label>
                <Col sm={10}>
                    <Form.Control as="select"
                                  name={inputFieldName}
                                  onChange={this.onChange}>
                        {inputFieldOptions}
                    </Form.Control>

                    <ErrorMessageList fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

class FormFilefield extends React.Component {

    constructor(props) {
        super(props);
        this.bubbleEvent = this.bubbleEvent.bind(this);
    }

    bubbleEvent(e) {
        this.props.handleFieldChange(e.target.name, e.target.files[0])
    }

    componentDidMount() {
        bsCustomFileInput.init();
    }

    render() {
        let controlId = this.props.name;
        let labelText = this.props.label === undefined ? this.props.name : this.props.label;
        let inputFieldName = this.props.name;
        let inputFieldPlaceholder = this.props.placeholder === undefined ? "Select a file" : this.props.placeholder;
        let inputFieldButtonText = this.props.button === undefined ? "Search" : this.props.button;
        let inputFieldErrorMessages = this.props.fieldErrors[this.props.name];

        return (
            <Form.Group as={Row} controlId={controlId}>
                <Form.Label column sm={2}>{labelText}</Form.Label>
                <Col sm={10}>
                    <Form.File custom>
                        <Form.File.Input isValid
                                         name={inputFieldName}
                                         onChange={this.bubbleEvent} />
                        <Form.File.Label data-browse={inputFieldButtonText}>
                            {inputFieldPlaceholder}
                        </Form.File.Label>
                    </Form.File>

                    <ErrorMessageList fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

class ErrorMessageList extends React.Component {

    render() {
        let doNotRenderComponent = this.props.fieldErrors === undefined || this.props.fieldErrors === null;

        if (doNotRenderComponent) {
            return null;
        }

        let backgroundClass = this.props.backgroundClass === undefined ? "bg-danger" : this.props.backgroundClass;

        return (
            <div>
                <ul className="error-list">
                    {this.props.fieldErrors.map((message, index) =>
                        <ErrorListItem key={index} backgroundClass={backgroundClass} message={message} />)}
                </ul>
            </div>
        );
    }
}

class ErrorListItem extends React.Component {

    render() {
        return (
            <li className="error-list-item">
                <Form.Text className={this.props.backgroundClass + " error"}>
                    <GoFlame /> {this.props.message}
                </Form.Text>
            </li>
        );
    }
}

export {
    FormInputfield,
    FormSelectfield,
    FormFilefield,
    ErrorMessageList
}
