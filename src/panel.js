var ReactComponent = require('./react_component');
var Prolog = require('proscript');

// Panel is rendered as either:
// <div>
//   <div class="fieldset_legend>
//     <div class="pre_fieldset"/>
//     <div class="fieldset_text"/>
//     <div class="post_fieldset"/>
//   </div>
//   <div {contentElement}.../>
// </div>

// or

// <div {contentElement}.../>

// The domNode is always the root element.

function Panel()
{
    ReactComponent.call(this);
    this.baseClassName = "proactive_container";
    this.contentElement = document.createElement("div");
    this.setDOMNode(this.contentElement);
    this.delete_fieldset = new RegExp('(\\s|^)fieldset(\\s|$)');
    this.delete_layout = new RegExp('(\\s|^)(vertical|horizontal)_layout(\\s|$)');
    this.legendElement = null;
    this.fieldSetTextElement = null;
}

Panel.prototype = new ReactComponent;


Panel.prototype.setProperties = function(t)
{
    ReactComponent.prototype.setProperties.call(this, t);
    if (t.label !== undefined && !ReactComponent.isNull(t.label))
    {
        if (this.legendElement == null)
        {
            this.legendElement = document.createElement("div");
            this.legendElement.className = "proactive_container horizontal_fill horizontal_layout fieldset_legend";
            var pre = document.createElement("div"); // this is the --- at the top-left
            pre.className = "pre_fieldset";
            this.legendElement.appendChild(pre);
            this.fieldSetTextElement = document.createElement("div"); // this is the text itself
            this.fieldSetTextElement.className = "fieldset_text";
            this.fieldSetTextElement.textContent = Prolog._portray(t.label);
            this.legendElement.appendChild(this.fieldSetTextElement);
            var post = document.createElement("div"); // this is the --- at the top-right
            post.className = "post_fieldset horizontal_fill";
            this.legendElement.appendChild(post);
            this.baseClassName = "proactive_container fieldset";
            var f = "no_fill";
            if (this.fill == "horizontal")
                f = "horizontal_fill";
            else if (this.fill == "vertical")
                f = "vertical_fill";
            var newContentElement = document.createElement("div");
            newContentElement.className = "proactive_container vertical_layout " + f;
            while (this.getDOMNode().firstChild != null)
                newContentElement.appendChild(this.contentElement.firstChild);
            this.getDOMNode().appendChild(this.legendElement);
            this.getDOMNode().appendChild(newContentElement);
            this.contentElement = newContentElement;
            this.configureHeight();
        }
        else
        {
            this.fieldSetTextElement.textContent = Prolog._portray(t.label);
        }
        this.restyle();
    }
    else if (t.label !== undefined && ReactComponent.isNull(t.label))
    {
        // Delete the legend if preset
        if (this.legendElement != null)
        {
            this.domNode.removeChild(this.legendElement);
            this.legendElement = null;
            while (this.contentElement.firstChild != null)
                this.getDOMNode().appendChild(this.contentElement.firstChild);
            this.contentElement = this.getDOMNode();
            this.configureHeight();
            this.baseClassName = "proactive_container";
            this.restyle();
        }
    }
    if (t.background !== undefined)
    {
        if (ReactComponent.isNull(t.background))
            this.getDOMNode().style.background = "inherit";
        else if (Prolog._is_atom(t.background))
            this.getDOMNode().style.background = Prolog._atom_chars(t.background);
    }
    if (t.scroll !== undefined)
    {
        if (ReactComponent.isNull(t.scroll))
            this.baseClassName = "proactive_container";
        else if (Prolog._portray(t.scroll) == "both")
            this.baseClassName = "proactive_container scrollpane scroll";
        else if (Prolog._portray(t.scroll) == "horizontal")
            this.baseClassName = "proactive_container scrollpane scrollx";
        else if (Prolog._portray(t.scroll) == "vertical")
            this.baseClassName = "proactive_container scrollpane scrolly";
        this.restyle();
    }
}

Panel.prototype.restyle = function()
{
    ReactComponent.prototype.restyle.call(this);
    this.configureHeight();
    this.domNode.className = this.getStyle().replace(this.delete_layout, ' vertical_layout ')
    this.contentElement.className = this.getStyle().replace(this.delete_fieldset, ' fieldset_main ');
}

Panel.prototype.appendChild = function(t)
{
    this.contentElement.appendChild(t.getDOMNode());
    t.setParent(this);
    this.configureHeight();
}

Panel.prototype.insertBefore = function(t, s)
{
    this.contentElement.insertBefore(t.getDOMNode(), s.getDOMNode());
    t.setParent(this);
    this.configureHeight();
}

Panel.prototype.replaceChild = function(n, o)
{
    this.contentElement.replaceChild(n.getDOMNode(), o.getDOMNode());
    n.setParent(this);
    o.setParent(null);
    this.configureHeight();
}


Panel.prototype.removeChild = function(t)
{
    this.contentElement.removeChild(t.getDOMNode());
    t.setParent(null);
    this.configureHeight();
}

Panel.prototype.configureHeight = function ()
{
    // If the panel contains a scrollpane then it can be crushed to effectively zero size
    // If we use min-content, then the panel will be sized to include the scrollable object as if it
    // had no overflow - ie the scrollpane will never appear
    // Similarly, if we always set it to 0 then things like buttons can get squashed into oblivion by a large table
    if (this.contentElement.getElementsByClassName("scrollpane").length == 0 && this.layout == "horizontal")
        this.contentElement.style["min-height"] = "min-content";
    else
        this.contentElement.style["min-height"] = "0";
}

module.exports = Panel;
