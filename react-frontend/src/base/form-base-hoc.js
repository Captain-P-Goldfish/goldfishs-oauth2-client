import React from "react";
import Form from "react-bootstrap/Form";
import {ErrorMessageList} from "../formfields/formInputfield";
import {Alert} from "react-bootstrap";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button";
import Spinner from "react-bootstrap/Spinner";
import {GoThumbsup} from "react-icons/go";

export default function FormBaseHoc(WrappedComponent) {

    return class extends React.Component {

        constructor(props) {
            super(props);
            this.state = {showSpinner: false, fields: {}, fieldErrors: {}};
            this.startSpinner = this.startSpinner.bind(this);
            this.stopSpinner = this.stopSpinner.bind(this);
            this.setFieldParam = this.setFieldParam.bind(this);
            this.getFieldErrors = this.getFieldErrors.bind(this);
            this.getFormData = this.getFormData.bind(this);
            this.onSuccessResponse = this.onSuccessResponse.bind(this);
            this.onErrorResponse = this.onErrorResponse.bind(this);
            this.submitForm = this.submitForm.bind(this);
        }

        shouldComponentUpdate(nextProps, nextState, nextContext) {
            if (this.props !== nextProps) {
                return true;
            } else if (this.state.fieldErrors !== nextState.fieldErrors) {
                return true;
            } else if (this.state.errorMessages !== nextState.errorMessages) {
                return true;
            } else if (this.state.success !== nextState.success) {
                return true;
            } else if (this.state.showSpinner !== nextState.showSpinner) {
                return true;
            }
            return false;
        }

        setErrors(errors) {
            this.setState({errorMessages: errors})
        }

        setFieldErrors(errors) {
            if (errors !== undefined && errors !== null && typeof errors === 'object') {
                this.setState({fieldErrors: errors})
            } else {
                this.setState({fieldErrors: undefined})
            }
        }

        getFieldErrors(name) {
            if (this.state.fieldErrors && this.state.fieldErrors[name] && this.state.fieldErrors[name] !== null) {
                return this.state.fieldErrors[name];
            }
        }

        startSpinner() {
            this.setState({showSpinner: true})
        }

        stopSpinner() {
            this.setState({showSpinner: false})
        }

        setFieldParam(name, value) {
            let stateObject = function () {
                let returnObj = {fields: this.state.fields};
                returnObj["fields"][name] = value;
                return returnObj;
            };
            this.setState(stateObject);
        }

        getFormData() {
            let data = new FormData();
            for (let [key, value] of Object.entries(this.state.fields)) {
                data.append(key, value);
            }
            return data;
        }

        onSuccessResponse(status, json) {
            this.setState({success: true, fieldErrors: {}, errorMessages: undefined});
            if (this.props.onSubmitSuccess !== undefined) {
                this.props.onSubmitSuccess(status, json);
            }
        }

        onErrorResponse(status, json) {
            if (json.errors !== undefined) {
                this.setFieldErrors(json.errors);
            } else if (json.errorMessages !== undefined && json.errorMessages !== null &&
                json.errorMessages.length > 0) {
                this.setState({
                    errorMessages: json.errorMessages,
                    fieldErrors: {}
                });
            } else {
                let errorMessages = ["[Unexpected error]"];
                Object.keys(json).forEach(function (key) {
                    let value = String(json[key]);
                    if (value !== undefined && value != null && value.length > 0) {
                        errorMessages.push(key + ": " + value);
                    }
                });
                this.setState({
                    errorMessages: errorMessages,
                    fieldErrors: {}
                });
            }
            this.setState({success: false})
        }

        submitForm(e) {
            e.preventDefault();

            let data = this.getFormData();
            let httpMethod = this.props.httpMethod === undefined ? "GET" : this.props.httpMethod;

            this.startSpinner();

            fetch(this.props.formUrl, {
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

        render() {
            let spinner;
            if (this.state.showSpinner) {
                spinner = <span style={{marginRight: 5 + 'px'}}>
                              <Spinner animation="border" variant="warning" size="sm" role="status" />
                          </span>;
            }

            let buttonText = this.props.buttonText === undefined ? "Submit" : this.props.buttonText;
            let className = this.props.disabled === undefined || this.props.disabled === false ? "" : "disabled";

            return (
                <Form id={this.props.formId} className={className} onSubmit={this.submitForm}>
                    <h2>{this.props.header}</h2>

                    <Alert variant={"success"} show={this.state.success === true}>
                        <Form.Text>
                            <GoThumbsup /> {this.props.successMessage}
                        </Form.Text>
                    </Alert>

                    <Alert variant={"danger"} show={this.state.errorMessages !== undefined}>
                        <ErrorMessageList fieldErrors={this.state.errorMessages} backgroundClass={""} />
                    </Alert>

                    <WrappedComponent setFieldParam={this.setFieldParam}
                                      fieldErrors={this.state.fieldErrors} {...this.props} />

                    <Form.Group as={Row}>
                        <Col sm={{span: 10, offset: 2}}>
                            <Button type="submit" onClick={this.startSpinner}>
                                {spinner} {buttonText}
                            </Button>
                        </Col>
                    </Form.Group>
                </Form>
            );
        }
    }
}