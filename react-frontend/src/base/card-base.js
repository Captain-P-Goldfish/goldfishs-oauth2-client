import React from "react";
import Form from "react-bootstrap/Form";
import {Optional} from "../services/utils";
import {GoFlame} from "react-icons/go";


export class CardInputField extends React.Component
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
        let inputFieldType = this.props.type === undefined ? "text" : this.props.type;
        let inputFieldName = this.props.name;
        let inputFieldPlaceholder = this.props.placeholder === undefined ? this.props.name : this.props.placeholder;
        let inputFieldErrorMessages = new Optional(this.props.onError).map(val => val(this.props.name)).orElse([]);
        let isDisabled = this.props.disabled === true;
        let isReadOnly = this.props.readOnly === true;

        return (
            <React.Fragment>
                <Form.Control id={controlId}
                              className={"card-input-field"}
                              type={inputFieldType}
                              name={inputFieldName}
                              disabled={isDisabled}
                              readOnly={isReadOnly}
                              placeholder={inputFieldPlaceholder}
                              onChange={this.bubbleEvent}
                              value={this.props.value} />
                <ErrorMessageList controlId={this.props.name + "-error-list"}
                                  fieldErrors={inputFieldErrorMessages} />
            </React.Fragment>
        )
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