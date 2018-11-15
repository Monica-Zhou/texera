import { WorkflowUtilService } from './../../../service/workflow-graph/util/workflow-util.service';
import { JointUIService } from './../../../service/joint-ui/joint-ui.service';
import { DragDropService } from './../../../service/drag-drop/drag-drop.service';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OperatorLabelComponent } from './operator-label.component';
import { OperatorMetadataService } from '../../../service/operator-metadata/operator-metadata.service';
import { StubOperatorMetadataService } from '../../../service/operator-metadata/stub-operator-metadata.service';

import { CustomNgMaterialModule } from '../../../../common/custom-ng-material.module';
import { mockOperatorSchemaList } from '../../../service/operator-metadata/mock-operator-metadata.data';
import { By } from '@angular/platform-browser';
import { WorkflowActionService } from '../../../service/workflow-graph/model/workflow-action.service';
import { TourService } from 'ngx-tour-ng-bootstrap';
import { RouterTestingModule } from '@angular/router/testing';
import { TourNgBootstrapModule } from 'ngx-tour-ng-bootstrap';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HotObservable } from '../../../../../../node_modules/rxjs/internal/testing/HotObservable';
import { marbles } from '../../../../../../node_modules/rxjs-marbles';
import { by } from '../../../../../../node_modules/protractor';

describe('OperatorLabelComponent', () => {
  const mockOperatorData = mockOperatorSchemaList[0];
  let component: OperatorLabelComponent;
  let fixture: ComponentFixture<OperatorLabelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [OperatorLabelComponent],
      imports: [
        CustomNgMaterialModule,
        RouterTestingModule.withRoutes([]),
        TourNgBootstrapModule.forRoot(),
        NgbModule.forRoot()
      ],
      providers: [
        DragDropService,
        JointUIService,
        WorkflowUtilService,
        WorkflowActionService,
        { provide: OperatorMetadataService, useClass: StubOperatorMetadataService },
        TourService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OperatorLabelComponent);
    component = fixture.componentInstance;

    // use one mock operator schema as input to construct the operator label
    component.operator = mockOperatorData;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should generate an ID for the component DOM element', () => {
    expect(component.operatorLabelID).toContain('texera-operator-label-');
  });

  it('should display operator user friendly name on the UI', () => {
    const element = <HTMLElement>(fixture.debugElement.query(By.css('.texera-operator-label-body')).nativeElement);
    expect(element.innerHTML.trim()).toEqual(mockOperatorData.additionalMetadata.userFriendlyName);
  });

  it('should register itself as a draggable element', () => {
    const jqueryElement = jQuery(`#${component.operatorLabelID}`);
    expect(jqueryElement.data('uiDraggable')).toBeTruthy();
  });

  it('should call the mouseEnter function once the cursor is hovering above a operator label', () => {
    const spy = spyOn<any>(component, 'mouseEnter');
    const operatorLabelElement = fixture.debugElement.query(By.css('#' + component.operatorLabelID));
    operatorLabelElement.triggerEventHandler('mouseenter', component);
    expect(spy).toHaveBeenCalled();
  });

  it('should call the mouseLeave function once the cursor leaves a operator label', () => {
    const spy = spyOn<any>(component, 'mouseLeave');
    const operatorLabelElement = fixture.debugElement.query(By.css('#' + component.operatorLabelID));
    operatorLabelElement.triggerEventHandler('mouseleave', component);
    expect(spy).toHaveBeenCalled();
  });

  it('should emits a command to open an tooltip instance after 500ms delay', marbles((m) => {
    const expectedStream = m.hot('500ms -a-');
    const actualStream = component.getopenCommandsObservable().map(() => 'a');
    m.hot('-a-').do(() => component.mouseEnter()).subscribe();
    m.expect(actualStream).toBeObservable(expectedStream);
  }));

  it('should display a tooltip instance with the correct content when openCommandObservable$ emits a value', marbles((m) => {
    const operatorLabelElement = fixture.debugElement.query(By.css('#' + component.operatorLabelID));
    m.hot('-a-').do(() => component.mouseEnter()).subscribe();
    component.getopenCommandsObservable().subscribe(x => {
      if (operatorLabelElement.parent) {
        const tooltipInstance = operatorLabelElement.parent.childNodes[1].nativeNode;
        expect(tooltipInstance.innerText).toBe(mockOperatorData.additionalMetadata.operatorDescription);
      }
    });
  }));

  it('should not emits a command to open tooltip instance if the cursor has left the operator label', marbles((m) => {
    const expectedStream_1 = m.hot('-----');
    const actualStream_1 = component.getopenCommandsObservable().map(() => 'a');
    m.hot('-a-----').do(() => component.mouseEnter()).subscribe();
    m.hot('---b---').do(() => component.mouseLeave()).subscribe();
    m.expect(actualStream_1).toBeObservable(expectedStream_1);
  }));

  it('should hide the tooltip instance if cursor leaves the operator label', marbles((m) => {
    const operatorLabelElement = fixture.debugElement.query(By.css('#' + component.operatorLabelID));
    m.hot('-a-').do(() => component.mouseEnter()).subscribe();
    component.getopenCommandsObservable().subscribe(x => {
      if (operatorLabelElement.parent) {
        const tooltipInstance = operatorLabelElement.parent.childNodes[1].nativeNode;
        expect(tooltipInstance).not.toBeNull();
        expect(operatorLabelElement.parent.childNodes.length).toBe(2);
      }
    });

    m.hot('-b-').do(() => component.mouseLeave()).subscribe();
    if (operatorLabelElement.parent) {
      expect(operatorLabelElement.parent.childNodes.length).toBe(1);
    }
  }));

  // TODO: simulate drag and drop in tests, possibly using jQueryUI Simulate plugin
  //  https://github.com/j-ulrich/jquery-simulate-ext/blob/master/doc/drag-n-drop.md

});
