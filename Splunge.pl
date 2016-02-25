:-module('Splunge', []).

:- quasi_quotation_syntax(jsx).

render_Splunge(State, _Props, Form):-
        memberchk(order=Order, State),
        ( Order == default->
            Label1 = top,
            Label2 = bottom,
            Key1 = top,
            Key2 = bottom
        ; otherwise->
            Key1 = bottom,
            Key2 = top,
            Label1 = bottom,
            Label2 = top
        ),
        Foo = foox,
        Bar = barx,
        Form = {|jsx||
               <Panel>
               <Title label={State.order}/>
               <Button key={Key1} label={Label1} onClick={swap_labels}/>
               <Button key={Key2} label={Label2} onClick={swap_labels}/>
               </Panel>|}.

getInitialState_Splunge([order=default]).

swap_labels(State, _, [order=swapped]):- memberchk(order=default, State).
swap_labels(State, _, [order=default]):- memberchk(order=swapped, State).

