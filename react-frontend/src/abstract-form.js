import React from 'react';
import {ErrorMessageList} from "./formfields/formInputfield";
import {Alert} from "react-bootstrap";

export default class AbstractForm extends React.Component {

    constructor(props) {
        super(props);
        this.handleFormChange = this.handleFormChange.bind(this);
        this.setFile = this.setFile.bind(this);
        this.getErrorProperty = this.getErrorProperty.bind(this);
    }

    handleFormChange(event) {
        let fieldName = event.target.name;
        let fieldVal = event.target.value;
        this.setState({[fieldName]: fieldVal})
    }

    getErrorProperty(propertyName) {
        if (this.formHasErrors() === "false") {
            return null;
        }
        if (this.state.errors[propertyName] !== undefined && this.state.errors[propertyName] != null) {
            return this.state.errors[propertyName];
        }
    }

    formHasErrors() {
        if ((this.state.errors === undefined || this.state.errors === null)) {
            return "false";
        }
        return "true";
    }

    handleErrorMessage(status, response) {
        if (status >= 399 && status <= 599) {
            if (response.errors) {
                this.setState({
                    errors: response.errors
                });
            } else if (response.errorMessages !== undefined && response.errorMessages !== null &&
                response.errorMessages.length > 0) {
                this.setState({
                    errorMessages: response.errorMessages
                });
            }
        }
    }

}
