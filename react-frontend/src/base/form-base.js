import React, {useEffect} from "react";
import bsCustomFileInput from "bs-custom-file-input";
import Form from "react-bootstrap/Form";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import {Optional} from "../services/utils";
import {GoFlame} from "react-icons/go";
import {Alert, Button, Spinner} from "react-bootstrap";
import {PencilSquare, PlusSquare, Save, TrashFill, XLg, XSquare} from "react-bootstrap-icons";
import {CardInputField} from "./card-base";

/**
 * a simple input field that might also display error messages directly bound to this input field
 */
export class FormInputField extends React.Component
{

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
                                  value={this.props.value} />

                    <ErrorMessageList controlId={this.props.name + "-error-list"}
                                      fieldErrors={inputFieldErrorMessages} />
                </Col>
            </Form.Group>
        );
    }
}

/**
 * a select input field that might also display error messages directly bound to this input field
 */
export class FormSelectField extends React.Component
{

    render()
    {
        let labelText = this.props.label === undefined ? this.props.name : this.props.label;
        let inputFieldErrorMessages = this.props.onError(this.props.name);

        let inputFieldOptions = new Optional(this.props.options).map(options =>
        {
            return options.map((value) =>
            {
                return <option key={value}>{value}</option>
            });
        }).orElse([])


        return (
            <Form.Group as={Row} controlId={this.props.name}>
                <Form.Label column sm={2}>
                    {labelText}
                </Form.Label>
                <Col sm={10}>
                    <Form.Control as="select"
                                  name={this.props.name}>
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
 * a file-input field that might also display error messages directly bound to this input field
 */
export function FormFileField(props)
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
                                     name={props.name} />
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
 * displays error messages for a {@link ConfigPageForm} element
 */
export function ErrorMessageList(props)
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

export function LoadingSpinner(props)
{
    if (props.show)
    {
        return (
            <span style={{marginRight: 5 + 'px'}}>
              <Spinner animation="border" variant="warning" size="sm" role="status" />
            </span>
        )
    }
    else
    {
        return null;
    }
}

export function ErrorMessagesAlert(props)
{
    return (
        new Optional(props.errors).map(errors => errors.errorMessages)
                                  .filter(messages => messages.length > 0)
                                  .isPresent() &&
        <Alert id={"error-messages-alert"} variant={"danger"}
               show={props.errors.errorMessages !== undefined}>
            <ErrorMessageList fieldErrors={props.errors.errorMessages} backgroundClass={""} />
        </Alert>
    )
}

export function CardControlIcons(props)
{
    return (
        <div className="card-control-icons">
            {props.spinner}
            {
                props.editMode &&
                <React.Fragment>
                    <Save title={"save"} id={"save-icon-" + props.resource.id}
                          onClick={() =>
                          {
                              if (props.resource.id === undefined)
                              {
                                  props.createResource()
                              }
                              else
                              {
                                  props.updateResource(props.resource.id)
                              }
                          }}
                          style={{marginRight: 5 + 'px'}} />
                    {
                        props.resource.id !== undefined &&
                        <XLg title={"reset-edit"} id={"reset-update-icon-" + props.resource.id}
                             onClick={props.resetEditMode} style={{marginRight: 5 + 'px'}} />
                    }
                </React.Fragment>
            }
            {
                !props.editMode &&
                <PencilSquare title={"edit"} id={"update-icon-" + props.resource.id}
                              onClick={props.edit} style={{marginRight: 5 + 'px'}} />
            }
            <TrashFill title={"delete"} id={"delete-icon-" + props.resource.id}
                       onClick={props.showModal} />
        </div>
    );
}

export function CardDateRows(props)
{
    return (
        <React.Fragment>
            <tr>
                <th>Created</th>
                <td className={"card-value-cell"}>
                    {
                        new Optional(props.resource).map(val => val.meta).map(
                            val => val.created).map(val => new Date(val).toUTCString()).orElse(null)
                    }
                </td>
            </tr>
            <tr>
                <th>LastModified</th>
                <td className={"card-value-cell"}>
                    {
                        new Optional(props.resource).map(val => val.meta).map(
                            val => val.lastModified).map(val => new Date(val).toUTCString()).orElse(
                            null)
                    }
                </td>
            </tr>
        </React.Fragment>
    );
}

export function ModifiableCardEntry(props)
{

    return <tr>
        <th>{props.header}</th>
        <td id={"card-cell-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
            {
                props.editMode &&
                <CardInputField value={new Optional(props.resourceValue).orElse("")}
                                id={props.name + "-" + props.resourceId}
                                type={props.type}
                                name={props.name}
                                placeholder={props.placeholder}
                                onChange={props.onChange}
                                onError={props.onError} />
            }
            {
                !props.editMode &&
                props.resourceValue
            }
        </td>
    </tr>
}

export function ModifiableCardFileEntry(props)
{

    return <tr>
        <th>{props.header}</th>
        <td id={"provider-card-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
            {
                props.editMode &&
                <XSquare key={"remove-key"} type={"button"} className={"remove-index"}
                         onClick={e => props.onRemove(props.name, undefined)} />
            }
            <div className={"public-key-box " + new Optional(props.resourceValue).map(val => "light-border")
                                                                                 .orElse("")}>
                {props.resourceValue}
            </div>
            {
                props.editMode &&
                <CardInputField type={"file"}
                                name={props.name}
                                placeholder={props.placeholder}
                                onChange={props.onChange}
                                onError={props.onError} />
            }
        </td>
    </tr>
}

export function ModifiableCardList(props)
{
    let inputFieldErrorMessages = new Optional(props.onError).map(val => val(props.name)).orElse([]);

    return <tr>
        <th>{props.header}</th>
        <td id={"provider-card-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
            {
                props.editMode &&
                <React.Fragment>
                    {
                        new Optional(props.resourceValue)
                            .map(endpointArray =>
                            {
                                return endpointArray.map((endpoint, index) =>
                                {
                                    return (
                                        <div>
                                            <XSquare key={"remove-" + index} type={"button"} className={"remove-index"}
                                                     onClick={e => props.onRemove(index)} />
                                            <CardInputField
                                                key={index}
                                                className={"list-item"}
                                                value={new Optional(endpoint).orElse("")}
                                                id={props.name + "[" + index + "]"}
                                                name={props.name + "[" + index + "]"}
                                                placeholder={props.placeholder}
                                                onChange={props.onChange}
                                                onError={props.onError} />
                                        </div>
                                    )

                                })
                            })
                            .orElse([])
                    }
                    <ErrorMessageList controlId={props.name + "-error-list"}
                                      fieldErrors={inputFieldErrorMessages} />
                    <Button key={"add"} type={"button"} className={"add-item"} onClick={props.onAdd}>
                        <PlusSquare /> Add new
                    </Button>
                </React.Fragment>
            }
            {
                !props.editMode &&
                new Optional(props.resourceValue)
                    .map(endpointArray =>
                    {
                        return (
                            <ul>
                                {
                                    endpointArray.map(endpoint =>
                                    {
                                        return (<li key={endpoint}>{endpoint}</li>)
                                    })
                                }
                            </ul>
                        )
                    }).orElse([])
            }
        </td>
    </tr>
}
