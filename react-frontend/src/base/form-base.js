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
export function FormInputField(props)
{

    let controlId = props.name;
    let label = new Optional(props.label).map(label => <Form.Label column sm={2}>{label}</Form.Label>);
    let inputFieldType = props.type === undefined ? "text" : props.type;
    let inputFieldName = props.name;
    let inputFieldPlaceholder = props.placeholder === undefined ? props.name : props.placeholder;
    let inputFieldErrorMessages = props.onError(props.name);
    let isDisabled = props.disabled === true;
    let isReadOnly = props.readOnly === true;
    let isHidden = props.type === "hidden" || props.isHidden;
    let as = new Optional(props.as).orElse("input");

    let sm = new Optional(props.sm).orElse(label.isPresent() ? 10 : 12);
    return (
        <Form.Group as={Row} controlId={controlId} style={{display: isHidden ? "none" : ""}}>
            {label.get()}
            <Col sm={sm}>
                <Form.Control type={inputFieldType}
                              as={as}
                              name={inputFieldName}
                              disabled={isDisabled}
                              readOnly={isReadOnly}
                              placeholder={inputFieldPlaceholder}
                              onChange={props.onChange}
                              value={props.value} />

                {
                    props.children
                }

                <ErrorMessageList controlId={props.name + "-error-list"}
                                  fieldErrors={inputFieldErrorMessages} />
            </Col>
        </Form.Group>
    );
}

/**
 * a simple input field that might also display error messages directly bound to this input field
 */
export class FormCheckbox extends React.Component
{

    render()
    {
        let controlId = new Optional(this.props.id).orElse(this.props.name);
        let label = new Optional(this.props.label).map(label => <Form.Label column sm={2}>{label}</Form.Label>);
        let inputFieldName = this.props.name;
        let inputFieldErrorMessages = this.props.onError(this.props.name);
        let isHidden = this.props.type === "hidden";
        let checked = new Optional(this.props.checked).orElse(false);

        let sm = new Optional(this.props.sm).orElse(label.isPresent() ? 10 : 12);
        return (
            <Form.Group as={Row} style={{display: isHidden ? "none" : ""}}>
                {label.get()}
                <Col sm={sm} style={{alignSelf: "center"}}>
                    <Form.Check id={controlId}
                                name={inputFieldName}
                                type="switch"
                                onChange={this.props.onChange}
                                checked={checked} />

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
 * a file-input field that might also display error messages directly bound to this input field
 */
export function FormObjectList(props)
{

    let labelText = props.label === undefined ? props.name : props.label;
    let inputFieldErrorMessages = props.onError(props.name);

    return (
        <Form.Group as={Row} controlId={props.name}>
            <Form.Label column sm={2}>{labelText}</Form.Label>
            <Col sm={10}>
                <Form.Control as="select"
                              size="sm"
                              type={"number"}
                              custom
                              name={props.name}
                              onChange={props.onChange}
                              value={props.selected}>
                    {
                        props.selections.map((value, index) =>
                        {
                            return (
                                <option key={index}
                                        value={value.id}
                                        defaultValue={props.selected === value}>
                                    {value.value}
                                </option>
                            )
                        })
                    }
                </Form.Control>

                <ErrorMessageList controlId={props.name + "-error-list"}
                                  fieldErrors={inputFieldErrorMessages} />
            </Col>
        </Form.Group>
    );
}

/**
 * a file-input field that might also display error messages directly bound to this input field
 */
export function FormRadioSelection(props)
{

    let labelText = props.label === undefined ? props.name : props.label;
    let displayType = new Optional(props.displayType).orElse("vertical")
    let displayClass = displayType === "vertical" ? "block" : "inline";
    let inputFieldErrorMessages = props.onError(props.name);

    return (
        <Form.Group as={Row} controlId={props.name}>
            <Form.Label column sm={2}>{labelText}</Form.Label>
            <Col sm={10} style={{alignSelf: "center"}}>
                {
                    props.selections.map((object, index) =>
                    {
                        return <Form.Check key={index}
                                           style={{display: displayClass, marginRight: "45px"}}
                                           type="radio"
                                           label={object.display}
                                           value={object.value}
                                           name={props.name}
                                           checked={props.selected === object.value}
                                           onChange={props.onChange}
                                           id={props.name + "-" + object.value}
                        />
                    })
                }
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
                <ErrorListItem key={"error-message-" + index} backgroundClass={backgroundClass} message={message} />)}
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

export function HiddenCardEntry(props)
{
    return <tr style={{display: "none"}}>
        <td>
            <CardInputField value={new Optional(props.value).orElse("")}
                            id={props.name + "-" + props.resourceId}
                            type={props.type}
                            name={props.name}
                            placeholder={props.placeholder}
                            onChange={props.onChange}
                            onError={props.onError} />
        </td>
    </tr>
}

export function ModifiableCardFileEntry(props)
{

    return <tr>
        <th>{props.header}</th>
        <td id={"card-cell-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
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
                                id={props.name + "-" + props.resourceId}
                                name={props.name}
                                placeholder={props.placeholder}
                                onChange={props.onChange}
                                onError={props.onError} />
            }
        </td>
    </tr>
}

export function CardRadioSelector(props)
{
    let inputFieldErrorMessages = new Optional(props.onError).map(val => val(props.name)).orElse([]);

    return <tr>
        <th>{props.header}</th>
        <td id={"card-cell-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
            <fieldset>
                {
                    !props.editMode &&
                    props.selected
                }
                {
                    props.editMode &&
                    props.selections.map((value, index) =>
                    {
                        return (
                            <Form.Check
                                key={index}
                                type="radio"
                                label={value}
                                value={value}
                                checked={props.selected === value}
                                onChange={props.onChange}
                                name={props.name}
                                id={props.name + "-" + value}
                            />
                        )
                    })
                }
            </fieldset>
            <ErrorMessageList controlId={props.name + "-error-list"}
                              fieldErrors={inputFieldErrorMessages} />
        </td>
    </tr>
}

export function CardListSelector(props)
{
    let inputFieldErrorMessages = new Optional(props.onError).map(val => val(props.name)).orElse([]);
    return <tr>
        <th>{props.header}</th>
        <td id={"card-cell-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
            <fieldset>
                {
                    !props.editMode &&
                    props.selected
                }
                {
                    props.editMode &&
                    <Form.Control as="select"
                                  size="sm"
                                  custom
                                  name={props.name}
                                  id={props.name}
                                  onChange={props.onChange}
                                  value={props.selected}
                    >
                        {
                            props.selections.map((value, index) =>
                            {
                                return (
                                    <option key={index}
                                            defaultValue={props.selected === value}>
                                        {value}
                                    </option>
                                )
                            })
                        }
                    </Form.Control>
                }
            </fieldset>
            <ErrorMessageList controlId={props.name + "-error-list"}
                              fieldErrors={inputFieldErrorMessages} />
        </td>
    </tr>
}

export function ModifiableCardList(props)
{
    let inputFieldErrorMessages = new Optional(props.onError).map(val => val(props.name)).orElse([]);

    return <tr>
        <th>{props.header}</th>
        <td id={"card-cell-" + props.resourceId + "-" + props.name} className={"card-value-cell"}>
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
                                        <div key={props.name + "-container-" + props.resourceId + "-" + index}>
                                            <XSquare key={"remove-" + props.resourceId + "-" + index} type={"button"}
                                                     className={"remove-index"}
                                                     onClick={e => props.onRemove(index)} />
                                            <CardInputField
                                                key={props.name + "-" + props.resourceId + "-" + index}
                                                className={"list-item"}
                                                value={new Optional(endpoint).orElse("")}
                                                id={props.name + "-" + props.resourceId + "-" + index}
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
