// Generated by the Scala Plugin for the Protocol Buffer Compiler.
// Do not edit!
//
// Protofile syntax: PROTO3

package edu.uci.ics.texera.web.workflowruntimestate

@SerialVersionUID(0L)
final case class JobStateStore(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    error: _root_.scala.Predef.String = "",
    eid: _root_.scala.Long = 0L
    ) extends scalapb.GeneratedMessage with scalapb.lenses.Updatable[JobStateStore] {
    @transient
    private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0
    private[this] def __computeSerializedValue(): _root_.scala.Int = {
      var __size = 0
      
      {
        val __value = state.value
        if (__value != 0) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeEnumSize(1, __value)
        }
      };
      
      {
        val __value = error
        if (!__value.isEmpty) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeStringSize(2, __value)
        }
      };
      
      {
        val __value = eid
        if (__value != 0L) {
          __size += _root_.com.google.protobuf.CodedOutputStream.computeInt64Size(3, __value)
        }
      };
      __size
    }
    override def serializedSize: _root_.scala.Int = {
      var read = __serializedSizeCachedValue
      if (read == 0) {
        read = __computeSerializedValue()
        __serializedSizeCachedValue = read
      }
      read
    }
    def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {
      {
        val __v = state.value
        if (__v != 0) {
          _output__.writeEnum(1, __v)
        }
      };
      {
        val __v = error
        if (!__v.isEmpty) {
          _output__.writeString(2, __v)
        }
      };
      {
        val __v = eid
        if (__v != 0L) {
          _output__.writeInt64(3, __v)
        }
      };
    }
    def withState(__v: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState): JobStateStore = copy(state = __v)
    def withError(__v: _root_.scala.Predef.String): JobStateStore = copy(error = __v)
    def withEid(__v: _root_.scala.Long): JobStateStore = copy(eid = __v)
    def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = {
      (__fieldNumber: @_root_.scala.unchecked) match {
        case 1 => {
          val __t = state.javaValueDescriptor
          if (__t.getNumber() != 0) __t else null
        }
        case 2 => {
          val __t = error
          if (__t != "") __t else null
        }
        case 3 => {
          val __t = eid
          if (__t != 0L) __t else null
        }
      }
    }
    def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = {
      _root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)
      (__field.number: @_root_.scala.unchecked) match {
        case 1 => _root_.scalapb.descriptors.PEnum(state.scalaValueDescriptor)
        case 2 => _root_.scalapb.descriptors.PString(error)
        case 3 => _root_.scalapb.descriptors.PLong(eid)
      }
    }
    def toProtoString: _root_.scala.Predef.String = _root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)
    def companion = edu.uci.ics.texera.web.workflowruntimestate.JobStateStore
    // @@protoc_insertion_point(GeneratedMessage[edu.uci.ics.texera.web.JobStateStore])
}

object JobStateStore extends scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.JobStateStore] {
  implicit def messageCompanion: scalapb.GeneratedMessageCompanion[edu.uci.ics.texera.web.workflowruntimestate.JobStateStore] = this
  def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): edu.uci.ics.texera.web.workflowruntimestate.JobStateStore = {
    var __state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED
    var __error: _root_.scala.Predef.String = ""
    var __eid: _root_.scala.Long = 0L
    var _done__ = false
    while (!_done__) {
      val _tag__ = _input__.readTag()
      _tag__ match {
        case 0 => _done__ = true
        case 8 =>
          __state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(_input__.readEnum())
        case 18 =>
          __error = _input__.readStringRequireUtf8()
        case 24 =>
          __eid = _input__.readInt64()
        case tag => _input__.skipField(tag)
      }
    }
    edu.uci.ics.texera.web.workflowruntimestate.JobStateStore(
        state = __state,
        error = __error,
        eid = __eid
    )
  }
  implicit def messageReads: _root_.scalapb.descriptors.Reads[edu.uci.ics.texera.web.workflowruntimestate.JobStateStore] = _root_.scalapb.descriptors.Reads{
    case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
      _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage eq scalaDescriptor), "FieldDescriptor does not match message type.")
      edu.uci.ics.texera.web.workflowruntimestate.JobStateStore(
        state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.fromValue(__fieldsMap.get(scalaDescriptor.findFieldByNumber(1).get).map(_.as[_root_.scalapb.descriptors.EnumValueDescriptor]).getOrElse(edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED.scalaValueDescriptor).number),
        error = __fieldsMap.get(scalaDescriptor.findFieldByNumber(2).get).map(_.as[_root_.scala.Predef.String]).getOrElse(""),
        eid = __fieldsMap.get(scalaDescriptor.findFieldByNumber(3).get).map(_.as[_root_.scala.Long]).getOrElse(0L)
      )
    case _ => throw new RuntimeException("Expected PMessage")
  }
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = WorkflowruntimestateProto.javaDescriptor.getMessageTypes().get(8)
  def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = WorkflowruntimestateProto.scalaDescriptor.messages(8)
  def messageCompanionForFieldNumber(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = throw new MatchError(__number)
  lazy val nestedMessagesCompanions: Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]] = Seq.empty
  def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = {
    (__fieldNumber: @_root_.scala.unchecked) match {
      case 1 => edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState
    }
  }
  lazy val defaultInstance = edu.uci.ics.texera.web.workflowruntimestate.JobStateStore(
    state = edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState.UNINITIALIZED,
    error = "",
    eid = 0L
  )
  implicit class JobStateStoreLens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.JobStateStore]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.JobStateStore](_l) {
    def state: _root_.scalapb.lenses.Lens[UpperPB, edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState] = field(_.state)((c_, f_) => c_.copy(state = f_))
    def error: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Predef.String] = field(_.error)((c_, f_) => c_.copy(error = f_))
    def eid: _root_.scalapb.lenses.Lens[UpperPB, _root_.scala.Long] = field(_.eid)((c_, f_) => c_.copy(eid = f_))
  }
  final val STATE_FIELD_NUMBER = 1
  final val ERROR_FIELD_NUMBER = 2
  final val EID_FIELD_NUMBER = 3
  def of(
    state: edu.uci.ics.texera.web.workflowruntimestate.WorkflowAggregatedState,
    error: _root_.scala.Predef.String,
    eid: _root_.scala.Long
  ): _root_.edu.uci.ics.texera.web.workflowruntimestate.JobStateStore = _root_.edu.uci.ics.texera.web.workflowruntimestate.JobStateStore(
    state,
    error,
    eid
  )
  // @@protoc_insertion_point(GeneratedMessageCompanion[edu.uci.ics.texera.web.JobStateStore])
}